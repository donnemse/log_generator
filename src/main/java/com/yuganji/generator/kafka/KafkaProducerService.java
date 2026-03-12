package com.yuganji.generator.kafka;

import com.google.gson.Gson;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.yuganji.generator.db.KafkaSettings;
import com.yuganji.generator.db.KafkaSettingsRepository;
import com.yuganji.generator.model.EpsVO;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class KafkaProducerService {

    @Autowired
    private KafkaSettingsRepository kafkaSettingsRepository;

    private final ConcurrentHashMap<Integer, Producer<String, byte[]>> producers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> topics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> outputTypes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, EpsVO> producerEps = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    /**
     * Create and cache a KafkaProducer for the given logger.
     */
    /**
     * @return null if success, error message if failed
     */
    public String createProducer(int loggerId, String kafkaTopic) {
        if (kafkaTopic == null || kafkaTopic.trim().isEmpty()) {
            log.warn("No Kafka topic for logger {}", loggerId);
            return "Kafka topic is not configured. Set topic first.";
        }
        KafkaSettings settings = kafkaSettingsRepository.findById(1).orElse(null);
        if (settings == null || settings.getBootstrapServers() == null || settings.getBootstrapServers().trim().isEmpty()) {
            log.warn("Global Kafka settings not configured for logger {}", loggerId);
            return "Kafka bootstrap servers not configured. Set Kafka Settings first.";
        }
        Properties props = getKafkaProducerProperties(settings.getBootstrapServers());
        Producer<String, byte[]> producer = new KafkaProducer<>(props);
        producers.put(loggerId, producer);
        topics.put(loggerId, kafkaTopic.trim());
        outputTypes.put(loggerId, settings.getOutputType() != null ? settings.getOutputType() : "json");
        producerEps.put(loggerId, new EpsVO("kafka-" + loggerId));
        log.info("Kafka producer created for logger {} -> topic {}", loggerId, kafkaTopic);
        return null;
    }

    /**
     * Send a batch of events to Kafka. NEVER throws - catches all exceptions internally.
     * All config is cached at createProducer time - no DB access on hot path.
     */
    public void send(int loggerId, List<Map<String, Object>> batch) {
        try {
            Producer<String, byte[]> producer = producers.get(loggerId);
            if (producer == null) {
                return;
            }
            String topic = topics.get(loggerId);
            if (topic == null) {
                return;
            }

            String outputType = outputTypes.getOrDefault(loggerId, "json");
            if (outputType.equalsIgnoreCase("csv")) {
                sendCsv(producer, topic, batch);
            } else {
                sendJson(producer, topic, batch);
            }

            EpsVO eps = producerEps.get(loggerId);
            if (eps != null) {
                eps.addCnt(batch.size());
            }
        } catch (Exception e) {
            log.error("Kafka send failed for logger {}: {}", loggerId, e.getMessage());
        }
    }

    private void sendCsv(Producer<String, byte[]> producer, String topic, List<Map<String, Object>> batch) {
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setHeaderWritingEnabled(true);
        settings.setHeaders("");
        CsvWriter writer = new CsvWriter(settings);
        StringBuilder msgBuilder = new StringBuilder();

        Set<String> header = batch.get(0).keySet();
        msgBuilder.append(writer.writeHeadersToString(header)).append('\n');
        batch.forEach(row -> msgBuilder.append(writer.writeRowToString(row)).append('\n'));
        producer.send(new ProducerRecord<>(topic, msgBuilder.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void sendJson(Producer<String, byte[]> producer, String topic, List<Map<String, Object>> batch) {
        batch.forEach(row -> {
            producer.send(new ProducerRecord<>(topic, gson.toJson(row).getBytes(StandardCharsets.UTF_8)));
        });
    }

    /**
     * Flush and close the producer for the given logger.
     */
    public void closeProducer(int loggerId) {
        Producer<String, byte[]> producer = producers.remove(loggerId);
        topics.remove(loggerId);
        outputTypes.remove(loggerId);
        producerEps.remove(loggerId);
        if (producer != null) {
            try {
                producer.flush();
                producer.close(Duration.ofSeconds(5));
                log.info("Kafka producer closed for logger {}", loggerId);
            } catch (Exception e) {
                log.error("Error closing Kafka producer for logger {}: {}", loggerId, e.getMessage());
            }
        }
    }

    /**
     * Get EPS tracking map for monitoring.
     */
    public ConcurrentHashMap<Integer, EpsVO> getProducerEps() {
        return producerEps;
    }

    @PreDestroy
    public void closeAll() {
        log.info("Shutting down all Kafka producers...");
        producers.forEach((loggerId, producer) -> {
            try {
                producer.flush();
                producer.close(Duration.ofSeconds(5));
            } catch (Exception e) {
                log.error("Error closing Kafka producer for logger {}: {}", loggerId, e.getMessage());
            }
        });
        producers.clear();
        topics.clear();
        outputTypes.clear();
        producerEps.clear();
    }

    private Properties getKafkaProducerProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1024 * 128);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024L * 1024 * 64);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);
        props.put(ProducerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
        return props;
    }
}
