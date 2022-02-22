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
    
    private long eps;
    
    public Generator(LogQueueService queueService, LoggerPropertyInfo logger) {
        this.queueService = queueService;
        this.logger = logger;
        this.eps = logger.getLogger().getEps();
    }

    @Override
    public void run() {
        int cnt = 0;
        long time = System.currentTimeMillis();
        while(this.state) {
          Map<String, Object> map = logger.getLogger().generateLog();
          queueService.push(map);
          cnt++;
          try {
              if (eps < 1000) {
                  Thread.sleep(1000 / eps);
              } else if (cnt % 100 == 0){
                  long diff = System.currentTimeMillis() - time;
                  Thread.sleep((1000 - (eps / 100 * diff)) / eps / 100);
                  time = System.currentTimeMillis();
                  
              }
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
        if (this.state) {
            return 1;
        } else {
            return 0;
        }
    }


}
