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
        
        int cnt = 0;
        IntBound epsBounds = new IntBound(logger.getEps());
        long time = System.currentTimeMillis();
        int eps = epsBounds.randomInt();
        while (!res.isCancelled()) {
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
                    eps = epsBounds.randomInt();
                } else if (cnt % 100 == 0){
                    long diff = System.currentTimeMillis() - time;
                    if (eps / 100 * diff < 1000) {
                        Thread.sleep(1000 - (eps / 100 * diff));
                    } else {
                        Thread.sleep(0, 1);
                    }
                    time = System.currentTimeMillis();
                    eps = epsBounds.randomInt();
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
