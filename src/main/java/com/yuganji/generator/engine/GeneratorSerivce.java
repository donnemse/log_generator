package com.yuganji.generator.engine;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yuganji.generator.db.Logger;
import com.yuganji.generator.db.LoggerRepository;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.queue.QueueService;
import com.yuganji.generator.util.NetUtil;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GeneratorSerivce {
    
    @Autowired
    private QueueService queueService;
    
    private Map<Integer, Future<String>> cache;
    
    @Autowired
    private LoggerService loggerService;

    @Autowired
    private LoggerRepository loggerRepository;

    @Autowired
    private GeneratorExecutor generator;
    
    @PostConstruct
    private void init() {
        this.cache = new ConcurrentHashMap<>();
        for (Entry<Integer, LoggerDto> entry: loggerService.list().entrySet()){
            if (entry.getValue().getStatus() == 1) {
                    Logger logger = entry.getValue().toEntity();
                    logger.setIp("System");
                    this.start(logger);
                    log.debug("started generator by scheduler: " + entry.getKey());
            }
        }
    }
    
    @Scheduled(initialDelay = 1_000, fixedDelay = 10 * 1000)
    private void schedule() {
        for (Entry<Integer, LoggerDto> entry: loggerService.list().entrySet()){
            if (entry.getValue().getStatus() == 1 && 
                    (cache.get(entry.getKey()).isCancelled() || cache.get(entry.getKey()).isDone())) {
                this.exceptStop(entry.getKey(), NetUtil.getLocalHostIp());
                log.error("Stopped generator by unknown error: " + entry.getKey());
            }
        }
    }
    
    private void exceptStop(int id, String ip) {
        this.cache.get(id).cancel(true);
        this.cache.remove(id);
        this.queueService.removeProducerEps(id);
        loggerService.get(id).setStatus(0);
    }

    public SingleObjectResponse start(Logger logger) {
        logger =  loggerService.get(logger.getId(), logger.getIp()).toEntity();
        
        if (this.isRunning(logger.getId())) {
            String message = "Already running: " + logger.getName();
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    message, false);
        }
        Future<String> future = generator.run(loggerService.get(logger.getId()));
        
        this.cache.put(logger.getId(), future);
        log.debug(logger.getName() + " was started.");
        loggerService.get(logger.getId()).setStatus(1);
        this.updateLoggerStatus(logger.getId(), 1, logger.getIp());

        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully started: " + logger.getName(), logger);
    }
    
    public SingleObjectResponse stop(Logger logger) {
        logger =  loggerService.get(logger.getId(), logger.getIp()).toEntity();
        
        if (this.cache.containsKey(logger.getId())) {
          log.debug(this.cache.get(logger.getId()).cancel(true));
          this.cache.remove(logger.getId());
          this.queueService.removeProducerEps(logger.getId());
        } else {
            String message = "Genrator was not running status: " + loggerService.get(logger.getId()).getName();
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    message, false);
        }
        
        loggerService.get(logger.getId()).setStatus(0);
        this.updateLoggerStatus(logger.getId(), 0, logger.getIp());
        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully stopped: " + loggerService.get(logger.getId()).getName(), logger);
    }
    
    
    
    public boolean isRunning(int id) {
        return this.cache.containsKey(id) && !this.cache.get(id).isCancelled();
    }

    private void updateLoggerStatus(int id, int status, String ip) {
        loggerRepository.setStatus(id, status, ip);
    }
}
