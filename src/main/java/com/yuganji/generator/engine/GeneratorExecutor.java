package com.yuganji.generator.engine;

import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.yuganji.generator.model.IntBound;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.queue.QueueService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GeneratorExecutor {
    
    @Autowired
    private QueueService queueService;
    
    @PostConstruct
    public void init() {
        
    }
    
    @Async
    public Future<String> run(LoggerDto logger) {
        AsyncResult<String> res = new AsyncResult<String>("Result");

        IntBound epsBounds = new IntBound(logger.getEps());
        int eps = epsBounds.randomInt();
        long checkPoint = System.currentTimeMillis();
        int cnt = 0;
        while (!res.isCancelled()) {
            try {
                Map<String, Object> map = logger.getDetail().generateLog();
                queueService.push(map, logger.getId());
                eps = epsBounds.randomInt();

                if (++cnt >= eps) {
                    Thread.sleep(Math.max(0, 1000 - (System.currentTimeMillis() - checkPoint)));
                    checkPoint = System.currentTimeMillis();
                    cnt = 0;
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                break;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                break;
            }
        }
        log.debug("#############################3");
        log.debug("Thread stopped");
        log.debug("#############################3");
        return res;
    }
    
}
