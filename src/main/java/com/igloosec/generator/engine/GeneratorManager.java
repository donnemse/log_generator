package com.igloosec.generator.engine;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.igloosec.generator.mybatis.mapper.LoggerMapper;
import com.igloosec.generator.prop.LoggerPropertyInfo;
import com.igloosec.generator.prop.LoggerPropertyManager;
import com.igloosec.generator.queue.LogQueueService;
import com.igloosec.generator.restful.model.SingleObjectResponse;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GeneratorManager {
    @Autowired
    private LogQueueService queueService;
    
    private Map<Integer, AGenerator> cache;
    
    @Autowired
    private LoggerPropertyManager loggerPropMng;
    
    @Autowired
    private LoggerMapper mapper;
    
    @PostConstruct
    private void init() {
        this.cache = new ConcurrentHashMap<>();
    }
    
    @Scheduled(initialDelay = 1_000, fixedDelay = 60 * 1000)
    private void schedule() {
        
        for (Entry<Integer, LoggerPropertyInfo> entry: loggerPropMng.listLogger().entrySet()){
            if (entry.getValue().getStatus() == 1) {
                if (!cache.containsKey(entry.getKey()) || cache.get(entry.getKey()).checkStatus() == 0) {
                    this.start(entry.getKey(), "System by reboot");
                    log.debug("started generator by scheduler: " + entry.getKey());
                }
            }
        }
    }
    
    public SingleObjectResponse start(int id, String ip) {
        if (cache.containsKey(id)) {
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Already runnig: " + id, false);
        }
        LoggerPropertyInfo logger = loggerPropMng.getLogger(id);
        AGenerator gen = new Generator(queueService, logger);
        gen.startGenerator();
        this.cache.put(id, gen);
        log.debug(logger.getName() + " was started.");
        loggerPropMng.getLogger(id).setStatus(1);
        this.updateLoggerStatus(id, 1, ip);
        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully started: " + logger.getName(), true);
    }
    
    public SingleObjectResponse stop(int id, String ip) {
        
        if (this.cache.containsKey(id)) {
            this.cache.get(id).stopGenerator();
            this.cache.remove(id);
        } else {
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Genrator was not runnig status: " + id, false);
        }
        loggerPropMng.getLogger(id).setStatus(0);
        this.updateLoggerStatus(id, 0, ip);
        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully stopped: " + loggerPropMng.getLogger(id).getName(), true);
    }

    private void updateLoggerStatus(int id, int status, String ip) {
        mapper.updateLoggerStatus(id, status);
        mapper.insertHistory(id, ip, new Date().getTime(), status == 1? "started": "stopped");
    }
}
