package com.yuganji.generator.output.model;

import lombok.Data;

@Data
public class FileOutputConfig {
    private String path;
    private String filePrefix;
    private String outputType;
    private int fileRotationMin;
    private int maxSize;
    private int batchSize;
}
