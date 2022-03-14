package com.yuganji.generator.output.file;

import com.yuganji.generator.output.file.OutputFileWriter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@Builder
@EqualsAndHashCode(callSuper=false)
public class JsonOutputWriter extends OutputFileWriter {
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
