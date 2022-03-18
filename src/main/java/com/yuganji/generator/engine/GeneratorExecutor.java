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
import com.yuganji.generator.output.OutputService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GeneratorExecutor {
    
    @Autowired
    private OutputService outputService;
    
    @PostConstruct
    public void init() {
        
    }
    
    @Async
    public Future<String> run(LoggerDto logger) {
        int cnt = 0;
        IntBound epsBounds = new IntBound(logger.getEps()); 
        long time = System.currentTimeMillis();
        while(!Thread.interrupted()) {
            int eps = epsBounds.randomInt();
            try {
                Map<String, Object> map = logger.getDetail().generateLog();
                outputService.push(map, logger.getId());
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
//                this.stopGenerator();
            }
        }
        return new AsyncResult<String>("Result");
    }
    
}
