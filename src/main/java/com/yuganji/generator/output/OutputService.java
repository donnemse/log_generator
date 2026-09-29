package com.yuganji.generator.output;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yuganji.generator.db.KafkaSettings;
import com.yuganji.generator.db.KafkaSettingsRepository;
import com.yuganji.generator.kafka.KafkaProducerService;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.model.LoggerDto;

import lombok.extern.log4j.Log4j2;

/**
 * Facade that routes each logger's output to the configured sink
 * (Kafka or local JSONL file) and centralizes EPS aggregation so monitoring is
 * independent of the chosen destination.
 */
@Service
@Log4j2
public class OutputService implements OutputSink {

    public static final String TARGET_FILE = "file";
    public static final String TARGET_KAFKA = "kafka";

    @Autowired
    private KafkaProducerService kafkaSink;

    @Autowired
    private JsonlFileSink fileSink;

    @Autowired
    private KafkaSettingsRepository kafkaSettingsRepository;

    /** loggerId -> sink currently handling that logger. */
    private final ConcurrentHashMap<Integer, OutputSink> active = new ConcurrentHashMap<>();
    /** loggerId -> EPS counter (shared across all sink types). */
    private final ConcurrentHashMap<Integer, EpsVO> eps = new ConcurrentHashMap<>();

    @Override
    public String open(LoggerDto logger) {
        KafkaSettings settings = kafkaSettingsRepository.findById(1).orElse(null);
        String target = (settings != null && settings.getOutputTarget() != null
                && !settings.getOutputTarget().trim().isEmpty())
                ? settings.getOutputTarget().trim() : TARGET_KAFKA;

        OutputSink sink = TARGET_FILE.equalsIgnoreCase(target) ? fileSink : kafkaSink;
        String error = sink.open(logger);
        if (error != null) {
            return error;
        }
        active.put(logger.getId(), sink);
        eps.put(logger.getId(), new EpsVO(target + "-" + logger.getId()));
        log.info("Output opened for logger {} -> target {}", logger.getId(), target);
        return null;
    }

    @Override
    public void write(int loggerId, List<Map<String, Object>> batch) {
        OutputSink sink = active.get(loggerId);
        if (sink == null) {
            return;
        }
        sink.write(loggerId, batch);
        EpsVO e = eps.get(loggerId);
        if (e != null) {
            e.addCnt(batch.size());
        }
    }

    @Override
    public void close(int loggerId) {
        OutputSink sink = active.remove(loggerId);
        eps.remove(loggerId);
        if (sink != null) {
            sink.close(loggerId);
        }
    }

    /**
     * EPS tracking map for monitoring.
     */
    public ConcurrentHashMap<Integer, EpsVO> getEps() {
        return eps;
    }
}
