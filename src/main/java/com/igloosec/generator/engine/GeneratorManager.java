package com.igloosec.generator.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.prop.LoggerPropertyInfo;
import com.igloosec.generator.prop.LoggerPropertyManager;
import com.igloosec.generator.queue.LogQueueService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GeneratorManager {
    @Autowired
    private LogQueueService queueService;
    
    private Map<Integer, AGenerator> cache;
    
    @Autowired
    private LoggerPropertyManager loggerPropMng;
    
    @PostConstruct
    private void init() {
        this.cache = new ConcurrentHashMap<>();
    }
    
    public boolean start(int id) {
        if (cache.containsKey(id)) {
            return false;
        }
        LoggerPropertyInfo logger = loggerPropMng.getLogger(id);
        AGenerator gen = new Generator(queueService, logger);
        gen.startGenerator();
        this.cache.put(id, gen);
        log.debug(logger.getName() + " was started.");
        return true;
    }
    
    public boolean stop(int id) {
        if (this.cache.containsKey(id)) {
            this.cache.get(id).stopGenerator();
            this.cache.remove(id);
        } else {
            return false;
        }
        return true;
        
    }
}
