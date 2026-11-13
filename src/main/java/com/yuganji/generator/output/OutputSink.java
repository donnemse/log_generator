package com.yuganji.generator.output;

import java.util.List;
import java.util.Map;

import com.yuganji.generator.model.LoggerDto;

/**
 * Abstraction over an output destination (Kafka, local file, ...).
 * Implementations own only the "write" responsibility; EPS aggregation is
 * centralized in {@link OutputService}.
 */
public interface OutputSink {

    /**
     * Open/prepare the destination for the given logger.
     *
     * @return null on success, an error message on failure.
     */
    String open(LoggerDto logger);

    /**
     * Write a batch of events. Implementations MUST NOT throw - all errors are
     * caught and logged internally.
     */
    void write(int loggerId, List<Map<String, Object>> batch);

    /**
     * Flush and release any resources held for the given logger.
     */
    void close(int loggerId);
}
