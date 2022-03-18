package com.yuganji.generator.engine;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.util.NetUtil;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GeneratorManager {
    
    @Autowired
    private OutputService outputService;
    
    private Map<Integer, AGenerator> cache;
    
    @Autowired
    private LoggerService loggerService;

    @Autowired
    private LoggerRepository loggerRepository;

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
            if (entry.getValue().getStatus() == 1 && cache.get(entry.getKey()).checkStatus() == 0) {
                this.exceptStop(entry.getKey(), NetUtil.getLocalHostIp());
                log.debug("Stopped generator by unknown error: " + entry.getKey());
            }
        }
    }
    
    private void exceptStop(int id, String ip) {
        this.cache.get(id).stopGenerator();
        this.cache.remove(id);
        this.outputService.removeProducerEps(id);
        loggerService.get(id).setStatus(0);
        this.updateLoggerStatus(id, 0, ip);
    }

    public SingleObjectResponse start(Logger logger) {
        logger = loggerService.get(logger.getId()).toEntity();
        if (this.isRunning(logger.getId())) {
            String message = "Already running: " + logger.getName();
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    message, false);
        }

        AGenerator gen = new Generator(outputService, loggerService.get(logger.getId()));
        gen.startGenerator();
        this.cache.put(logger.getId(), gen);
        log.debug(logger.getName() + " was started.");
        loggerService.get(logger.getId()).setStatus(1);
        this.updateLoggerStatus(logger.getId(), 1, logger.getIp());

        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully started: " + logger.getName(), true);
    }
    
    public SingleObjectResponse stop(Logger logger) {
        int id = logger.getId();
        String ip = logger.getIp();
        if (this.cache.containsKey(id)) {
            this.cache.get(id).stopGenerator();
            this.cache.remove(id);
            this.outputService.removeProducerEps(id);
        } else {
            String message = "Genrator was not running status: " + loggerService.get(id).getName();
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    message, false);
        }
        loggerService.get(id).setStatus(0);
        this.updateLoggerStatus(id, 0, ip);
        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully stopped: " + loggerService.get(id).getName(), true);
    }
    
    public boolean isRunning(int id) {
        return this.cache.containsKey(id);
    }

    private void updateLoggerStatus(int id, int status, String ip) {
        loggerRepository.setStatus(id, status, ip);
    }
}
