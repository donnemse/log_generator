package com.igloosec.generator.engine;

import java.util.Map;

import com.igloosec.generator.prop.LoggerPropertyInfo;
import com.igloosec.generator.queue.LogQueueService;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Generator extends AGenerator {
    
    private LogQueueService queueService;
    private LoggerPropertyInfo logger;
    
    private volatile boolean state = true;
    
    public Generator(LogQueueService queueService, LoggerPropertyInfo logger) {
        this.queueService = queueService;
        this.logger = logger;
    }

    @Override
    public void run() {
        while(this.state) {
          Map<String, Object> b = logger.getLogger().generateLog();
          queueService.pushLog(b);
          log.debug(b);
          try {
              Thread.sleep(5000);
          } catch (InterruptedException e) {
              log.error(e.getMessage(), e);
          }
      }
    }

    @Override
    public boolean startGenerator() {
        this.start();
        return true;
    }

    @Override
    public boolean stopGenerator() {
        this.state = false;
        return false;
    }

    @Override
    public int checkStatus() {
        
        return 0;
    }


}
