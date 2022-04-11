package com.yuganji.generator.output.model;

import lombok.Data;

@Data
public class KafkaOutputConfig {
    private String bootstrapServers;
    private String topicName;
    private String outputType;
    private int batchSize;
}
