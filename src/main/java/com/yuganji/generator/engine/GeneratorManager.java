package com.yuganji.generator.engine;

import com.yuganji.generator.db.LoggerRepository;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.util.NetUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class GeneratorManager {
    private static final String TYPE = "logger";
//    @Autowired
//    private QueueService queueService;
    @Autowired
    private OutputService outputService;
    
    private Map<Integer, AGenerator> cache;
    
    @Autowired
    private LoggerService loggerPropMng;

    @Autowired
    private LoggerRepository loggerRepository;
//    @Autowired
//    private LoggerMapper loggerMapper;
//
//    @Autowired
//    private HistoryMapper histMapper;
    
    @PostConstruct
    private void init() {
        this.cache = new ConcurrentHashMap<>();
        
        for (Entry<Integer, LoggerDto> entry: loggerPropMng.list().entrySet()){
            if (entry.getValue().getStatus() == 1) {
                    this.start(entry.getKey(), NetUtil.getLocalHostIp());
                    log.debug("started generator by scheduler: " + entry.getKey());
//                }
            }
        }
    }
    
    @Scheduled(initialDelay = 1_000, fixedDelay = 10 * 1000)
    private void schedule() {
        
        for (Entry<Integer, LoggerDto> entry: loggerPropMng.list().entrySet()){
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
        loggerPropMng.get(id).setStatus(0);
        this.updateLoggerStatus(id, 0, ip);
//        histMapper.insertHistory(id, ip, TYPE, new Date().getTime(),
//                "Stopped logger by error. plz check yaml", null, null);
    }

    public SingleObjectResponse start(int id, String ip) {
        if (this.isRunning(id)) {
            String message = "Already running: " + loggerPropMng.get(id).getName();
//            histMapper.insertHistory(id, ip, TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    message, false);
        }
        LoggerDto logger = loggerPropMng.get(id);


        AGenerator gen = new Generator(outputService, logger);
        gen.startGenerator();
        this.cache.put(id, gen);
        log.debug(logger.getName() + " was started.");
        loggerPropMng.get(id).setStatus(1);
        this.updateLoggerStatus(id, 1, ip);

        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully started: " + logger.getName(), true);
    }
    
    public SingleObjectResponse stop(int id, String ip) {
        
        if (this.cache.containsKey(id)) {
            this.cache.get(id).stopGenerator();
            this.cache.remove(id);
            this.outputService.removeProducerEps(id);
        } else {
            String message = "Genrator was not running status: " + loggerPropMng.get(id).getName();
//            histMapper.insertHistory(id, ip, TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    message, false);
        }
        loggerPropMng.get(id).setStatus(0);
        this.updateLoggerStatus(id, 0, ip);
        return new SingleObjectResponse(
                HttpStatus.OK.value(), 
                "Successfully stopped: " + loggerPropMng.get(id).getName(), true);
    }
    
    public boolean isRunning(int id) {
        return this.cache.containsKey(id);
    }

    private void updateLoggerStatus(int id, int status, String ip) {
        loggerRepository.setStatus(id, status, ip);
//        loggerMapper.updateLoggerStatus(id, status);
        String message = "Successfully " + (status == 1? "started. ": "stopped. ") + loggerPropMng.get(id).getName();
//        histMapper.insertHistory(id, ip, TYPE, new Date().getTime(), message, null, null);
    }
}
