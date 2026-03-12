package com.yuganji.generator.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yuganji.generator.db.Logger;
import com.yuganji.generator.db.LoggerRepository;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.IntBound;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.kafka.KafkaProducerService;
import com.yuganji.generator.util.NetUtil;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GeneratorSerivce {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    private Map<Integer, List<Future<String>>> cache;

    private final ConcurrentHashMap<Integer, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private LoggerRepository loggerRepository;

    @Autowired
    private GeneratorExecutor generator;

    @PostConstruct
    private void init() {
        this.cache = new ConcurrentHashMap<>();
        for (Entry<Integer, LoggerDto> entry: loggerService.list().entrySet()){
            if (entry.getValue().getStatus() == 1) {
                    Logger logger = entry.getValue().toEntity();
                    logger.setIp("System");
                    this.start(logger);
                    log.debug("started generator by scheduler: " + entry.getKey());
            }
        }
    }

    @Scheduled(initialDelay = 1_000, fixedDelay = 10 * 1000)
    private void schedule() {
        for (Entry<Integer, LoggerDto> entry: loggerService.list().entrySet()){
            if (entry.getValue().getStatus() == 1 && cache.containsKey(entry.getKey())) {
                List<Future<String>> futures = cache.get(entry.getKey());
                boolean allDone = futures.stream().allMatch(f -> f.isCancelled() || f.isDone());
                if (allDone) {
                    this.exceptStop(entry.getKey(), NetUtil.getLocalHostIp());
                    log.error("Stopped generator by unknown error: " + entry.getKey());
                }
            }
        }
    }

    private void exceptStop(int id, String ip) {
        AtomicBoolean running = runningFlags.remove(id);
        if (running != null) {
            running.set(false);
        }
        List<Future<String>> futures = this.cache.remove(id);
        if (futures != null) {
            futures.forEach(f -> f.cancel(true));
        }
        kafkaProducerService.closeProducer(id);
        loggerService.get(id).setStatus(0);
    }

    public SingleObjectResponse start(Logger logger) {
        logger = loggerService.get(logger.getId(), logger.getIp()).toEntity();

        if (this.isRunning(logger.getId())) {
            String message = "Already running: " + logger.getName();
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message, false);
        }

        LoggerDto loggerDto = loggerService.get(logger.getId());

        // Calculate worker count based on max EPS
        IntBound epsBounds = new IntBound(loggerDto.getEps());
        int minEps = epsBounds.getMin();
        int maxEps = epsBounds.getMax() - 1;
        int workerCount = Math.max(1, Math.min(8, maxEps / 25000));

        // Create shared running flag for all workers of this logger
        AtomicBoolean running = new AtomicBoolean(true);
        runningFlags.put(logger.getId(), running);

        // Create Kafka producer before spawning workers
        String kafkaError = kafkaProducerService.createProducer(logger.getId(), loggerDto.getKafkaTopic());
        if (kafkaError != null) {
            runningFlags.remove(logger.getId());
            loggerService.get(logger.getId()).setStatus(0);
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    kafkaError, false);
        }

        // Spawn workers with proportional EPS range
        List<Future<String>> futures = new ArrayList<>();
        int perWorkerMin = minEps / workerCount;
        int perWorkerMax = maxEps / workerCount;
        for (int i = 0; i < workerCount; i++) {
            String workerEps;
            if (i == workerCount - 1) {
                // Last worker gets remainder
                int lastMin = minEps - perWorkerMin * (workerCount - 1);
                int lastMax = maxEps - perWorkerMax * (workerCount - 1);
                workerEps = lastMin == lastMax
                        ? String.valueOf(lastMax)
                        : lastMin + "~" + lastMax;
            } else {
                workerEps = perWorkerMin == perWorkerMax
                        ? String.valueOf(perWorkerMax)
                        : perWorkerMin + "~" + perWorkerMax;
            }
            futures.add(generator.run(loggerDto, workerEps, running));
        }

        this.cache.put(logger.getId(), futures);
        log.info("{} was started with {} worker(s), target EPS: {}",
                logger.getName(), workerCount, loggerDto.getEps());
        loggerService.get(logger.getId()).setStatus(1);
        this.updateLoggerStatus(logger.getId(), 1, logger.getIp());

        return new SingleObjectResponse(
                HttpStatus.OK.value(),
                "Successfully started: " + logger.getName(), logger);
    }

    public SingleObjectResponse stop(Logger logger) {
        logger = loggerService.get(logger.getId(), logger.getIp()).toEntity();

        if (this.cache.containsKey(logger.getId())) {
            // Signal all workers to stop
            AtomicBoolean running = runningFlags.remove(logger.getId());
            if (running != null) {
                running.set(false);
            }
            // Cancel all worker futures (sends interrupt to break Thread.sleep)
            List<Future<String>> futures = this.cache.remove(logger.getId());
            if (futures != null) {
                futures.forEach(f -> f.cancel(true));
            }
            kafkaProducerService.closeProducer(logger.getId());
        } else {
            String message = "Generator was not running status: " + loggerService.get(logger.getId()).getName();
            return new SingleObjectResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message, false);
        }

        loggerService.get(logger.getId()).setStatus(0);
        this.updateLoggerStatus(logger.getId(), 0, logger.getIp());
        return new SingleObjectResponse(
                HttpStatus.OK.value(),
                "Successfully stopped: " + loggerService.get(logger.getId()).getName(), logger);
    }

    public boolean isRunning(int id) {
        if (!this.cache.containsKey(id)) return false;
        AtomicBoolean running = runningFlags.get(id);
        return running != null && running.get();
    }

    private void updateLoggerStatus(int id, int status, String ip) {
        loggerRepository.setStatus(id, status, ip);
    }
}
