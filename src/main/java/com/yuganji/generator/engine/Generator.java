package com.yuganji.generator.engine;

import java.util.Map;

import com.yuganji.generator.model.IntBound;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.queue.QueueService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Deprecated
public class Generator extends AGenerator {
    
    private QueueService queueService;
    private LoggerDto logger;
    
    private volatile boolean state = true;
    
    private IntBound epsBounds;
    
    public Generator(QueueService queueService, LoggerDto logger) {
        this.queueService = queueService;
        this.logger = logger;
        this.epsBounds = new IntBound(logger.getEps());
    }

    @Override
    public void run() {
        int cnt = 0;
        long time = System.currentTimeMillis();
        while(this.state) {
            int eps = this.epsBounds.randomInt();
            try {
                Map<String, Object> map = logger.getDetail().generateLog();
                queueService.push(map, logger.getId());
                cnt++;
                if (eps < 1000) {
                    long t = System.currentTimeMillis() - time;
                    if (t >= 1000) {
                        time = System.currentTimeMillis();
                        continue;
                    }
                    Thread.sleep((1000 - t) / eps);
                    time = System.currentTimeMillis();
                } else if (cnt % 100 == 0){
                    long diff = System.currentTimeMillis() - time;
                    Thread.sleep((1000 - (eps / 100 * diff)) / eps / 100);
                    time = System.currentTimeMillis();
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                this.stopGenerator();
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
