package com.yuganji.generator.output.file;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper=false)
public class JsonAndRawOutputWriter extends OutputFileWriter {
    private String type;
    private int outputId;
    private String path;
    private String outputType;
    private int fileRotationMin;
    private String fileFormat;
    private String fileName;
    private int maxSize;
    private int batchSize;

    @Override
    public boolean startOutput() {
        return false;
    }

    @Override
    public boolean stopOutput() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
