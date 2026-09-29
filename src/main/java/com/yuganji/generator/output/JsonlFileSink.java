package com.yuganji.generator.output;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.yuganji.generator.db.KafkaSettings;
import com.yuganji.generator.db.KafkaSettingsRepository;
import com.yuganji.generator.model.LoggerDto;

import lombok.extern.log4j.Log4j2;

/**
 * Writes generated events as JSON Lines (one JSON object per line) to a single
 * shared local file: {outputDir}/{outputFile} (append mode). Every logger and
 * every worker thread writes to the same file. The writer is reference-counted:
 * it opens on the first active logger and closes when the last one stops.
 */
@Service
@Log4j2
public class JsonlFileSink implements OutputSink {

    private static final int BUFFER_SIZE = 256 * 1024;
    private static final String DEFAULT_DIR = "./output";
    private static final String DEFAULT_FILE = "output.jsonl";

    @Autowired
    private KafkaSettingsRepository kafkaSettingsRepository;

    private final Gson gson = new Gson();

    /** Guards writer lifecycle and all writes to the shared file. */
    private final Object lock = new Object();
    /** Loggers currently using the shared file (reference count). */
    private final Set<Integer> activeLoggers = ConcurrentHashMap.newKeySet();

    private volatile BufferedWriter writer;

    @Override
    public String open(LoggerDto logger) {
        KafkaSettings settings = kafkaSettingsRepository.findById(1).orElse(null);
        String dir = (settings != null && settings.getOutputDir() != null
                && !settings.getOutputDir().trim().isEmpty())
                ? settings.getOutputDir().trim() : DEFAULT_DIR;
        String fileName = (settings != null && settings.getOutputFile() != null
                && !settings.getOutputFile().trim().isEmpty())
                ? settings.getOutputFile().trim() : DEFAULT_FILE;
        synchronized (lock) {
            if (writer == null) {
                try {
                    Path dirPath = Paths.get(dir);
                    Files.createDirectories(dirPath);
                    Path file = dirPath.resolve(fileName);
                    writer = new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(file.toFile(), true), StandardCharsets.UTF_8),
                            BUFFER_SIZE);
                    log.info("JSONL file sink opened -> {}", file.toAbsolutePath());
                } catch (IOException e) {
                    log.error("Failed to open JSONL file in {}: {}", dir, e.getMessage());
                    return "Failed to open output file: " + e.getMessage();
                }
            }
            activeLoggers.add(logger.getId());
        }
        return null;
    }

    @Override
    public void write(int loggerId, List<Map<String, Object>> batch) {
        if (batch.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder(batch.size() * 256);
        for (Map<String, Object> row : batch) {
            sb.append(gson.toJson(row)).append('\n');
        }
        String data = sb.toString();
        synchronized (lock) {
            if (writer == null) {
                return;
            }
            try {
                writer.write(data);
            } catch (IOException e) {
                log.error("JSONL write failed for logger {}: {}", loggerId, e.getMessage());
            }
        }
    }

    @Override
    public void close(int loggerId) {
        synchronized (lock) {
            activeLoggers.remove(loggerId);
            if (activeLoggers.isEmpty() && writer != null) {
                try {
                    writer.flush();
                    writer.close();
                    log.info("JSONL file sink closed (last logger {} stopped)", loggerId);
                } catch (IOException e) {
                    log.error("Error closing JSONL file: {}", e.getMessage());
                } finally {
                    writer = null;
                }
            }
        }
    }

    /**
     * Periodically flush the buffer so a crash loses at most a few seconds of data.
     */
    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
    public void flushAll() {
        synchronized (lock) {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e) {
                    log.error("JSONL flush failed: {}", e.getMessage());
                }
            }
        }
    }

    @PreDestroy
    public void closeAll() {
        synchronized (lock) {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    log.error("Error closing JSONL file: {}", e.getMessage());
                } finally {
                    writer = null;
                }
            }
            activeLoggers.clear();
        }
    }
}
