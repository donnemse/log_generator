package com.yuganji.generator.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.yuganji.generator.model.IntBound;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.kafka.KafkaProducerService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GeneratorExecutor {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @PostConstruct
    public void init() {

    }

    @Async
    public Future<String> run(LoggerDto logger, String epsStr, AtomicBoolean running) {
        IntBound epsBounds = new IntBound(epsStr);
        int eps = epsBounds.randomInt();
        long checkPoint = System.currentTimeMillis();
        int cnt = 0;
        final int BATCH_SIZE = 100;
        List<Map<String, Object>> batch = new ArrayList<>(BATCH_SIZE);
        while (running.get()) {
            try {
                Map<String, Object> map = logger.getDetail().generateLog();
                batch.add(map);

                if (batch.size() >= BATCH_SIZE) {
                    kafkaProducerService.send(logger.getId(), batch);
                    batch = new ArrayList<>(BATCH_SIZE);
                }

                if (++cnt >= eps) {
                    if (!batch.isEmpty()) {
                        kafkaProducerService.send(logger.getId(), batch);
                        batch = new ArrayList<>(BATCH_SIZE);
                    }
                    Thread.sleep(Math.max(0, 1000 - (System.currentTimeMillis() - checkPoint)));
                    checkPoint = System.currentTimeMillis();
                    cnt = 0;
                    eps = epsBounds.randomInt();
                }
            } catch (InterruptedException e) {
                log.debug("Generator thread interrupted for logger: {}", logger.getName());
                break;
            } catch (Exception e) {
                log.error("Generator crashed for logger: {} (id={}), error: {}",
                        logger.getName(), logger.getId(), e.getMessage(), e);
                break;
            } catch (Error e) {
                log.error("Generator fatal error for logger: {} (id={}), error: {}",
                        logger.getName(), logger.getId(), e.getMessage(), e);
                break;
            }
        }
        if (!batch.isEmpty()) {
            kafkaProducerService.send(logger.getId(), batch);
        }
        log.debug("Generator thread stopped for logger: {}", logger.getName());
        return new AsyncResult<>("Completed");
    }
}
