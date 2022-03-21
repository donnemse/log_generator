package com.yuganji.generator.engine;

import com.yuganji.generator.model.IntBound;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.output.OutputService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.Future;

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
        int eps = epsBounds.randomInt();

        while(!Thread.interrupted()) {
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
                    eps = epsBounds.randomInt();
                } else if (cnt % 100 == 0){
                    long diff = System.currentTimeMillis() - time;
                    if (eps / 100 * diff < 1000) {
                        Thread.sleep(1000 - (eps / 100 * diff));
                    }
                    time = System.currentTimeMillis();
                    eps = epsBounds.randomInt();
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
