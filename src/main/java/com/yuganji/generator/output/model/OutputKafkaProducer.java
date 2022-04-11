package com.yuganji.generator.output.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.yuganji.generator.configuration.ApplicationContextProvider;
import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.queue.QueueService;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Log4j2
public class OutputKafkaProducer extends Thread implements IOutput {

    private final KafkaOutputConfig config;
    private final int outputId;

    public OutputKafkaProducer(int id, Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(
                new PropertyNamingStrategies.SnakeCaseStrategy());

        this.config = mapper.convertValue(map, KafkaOutputConfig.class);
        this.outputId = id;
    }

    @Override
    public boolean startOutput() throws OutputHandleException {
        super.setName("thread_output_" + outputId);
        super.start();
        return true;
    }

    @Override
    public boolean stopOutput() throws OutputHandleException {
        super.interrupt();
        return true;
    }

    @Override
    public boolean isRunning() throws OutputHandleException {
        return super.isAlive();
    }

    @Override
    public boolean isReadyForRunning() {
        return super.getState().equals(Thread.State.NEW);
    }

    @Override
    public void run() {
        QueueService queueService = ApplicationContextProvider.getApplicationContext().getBean(QueueService.class);

        Properties prop = this.getKafkaProducerProperties(config.getBootstrapServers());
        Producer<String, byte[]> producer = new KafkaProducer<>(prop);
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setHeaderWritingEnabled(true);
        settings.setHeaders("");
        CsvWriter writer = new CsvWriter(settings);
        StringBuilder msgBuilder = new StringBuilder();

        while (true) {
            try {
                List<Map<String, Object>> list = queueService.poll(this.outputId, this.config.getBatchSize());
                if (list.size() == 0) {
                    Thread.sleep(1_000);
                    continue;
                }
                Set<String> header = list.get(0).keySet();
                msgBuilder.append(writer.writeHeadersToString(header)).append('\n');
                list.forEach(row -> msgBuilder.append(writer.writeRowToString(row)).append('\n'));
                producer.send(
                        new ProducerRecord<>(config.getTopicName(), msgBuilder.toString().getBytes(StandardCharsets.UTF_8)));
                producer.flush();
                msgBuilder.setLength(0);


            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                producer.flush();
                producer.close();
                break;
            }
        }
    }

    private Properties getKafkaProducerProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1024 * 128);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024L * 1024 * 1024 * 5);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.GZIP.name);
        return props;
    }
}
