package com.yuganji.generator.output.model;

import com.yuganji.generator.exception.OutputHandleException;

import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.output.file.CsvOutputWriter;
import com.yuganji.generator.output.file.JsonAndRawOutputWriter;
import com.yuganji.generator.output.file.OutputFileWriter;
import com.yuganji.generator.output.file.RawOutputWriter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileOutput extends AbstractOutputHandler {
    private transient final String KEY_PATH = "path";
    private transient final String KEY_FILE_ROTATION_MIN = "file_rotation_min";
    private transient final String KEY_FILE_FORMAT = "file_format";
    private transient final String KEY_FILE_NAME = "file_name";
    private transient final String KEY_MAX_SIZE = "max_size";
    private transient final String KEY_BATCH_SIZE = "batch_size";

    private int id;
    private String path;
    private String outputType;
    private int fileRotationMin;
    private String fileFormat;
    private String fileName;
    private int maxSize;
    private int batchSize;
    private transient OutputFileWriter writer;

    public FileOutput(int id, Map<String, Object> conf) {
        this.id = id;
        this.path = conf.get(KEY_PATH).toString();
        this.fileRotationMin = (int) conf.get(KEY_FILE_ROTATION_MIN);
        this.fileFormat = conf.get(KEY_FILE_FORMAT).toString();
        this.fileName = conf.get(KEY_FILE_NAME).toString();
        this.maxSize = (int) conf.get(KEY_MAX_SIZE);
        this.batchSize = (int) conf.get(KEY_BATCH_SIZE);
    }

    @Override
    public boolean startOutput() throws OutputHandleException {
        if (this.writer == null) {
            if (fileFormat.equalsIgnoreCase("csv")) {
                this.writer = CsvOutputWriter.builder()
                        .outputId(this.id)
                        .type(this.fileFormat)
                        .path(this.path)
                        .fileRotationMin(this.fileRotationMin)
                        .fileFormat(this.fileFormat)
                        .fileName(this.fileName)
                        .maxSize(this.maxSize)
                        .batchSize(this.batchSize)
                        .build();
            } else if (fileFormat.equalsIgnoreCase("json") ||
                    fileFormat.equalsIgnoreCase("raw")) {
                this.writer = RawOutputWriter.builder()
                        .outputId(this.id)
                        .type(this.fileFormat)
                        .path(this.path)
                        .fileRotationMin(this.fileRotationMin)
                        .fileFormat(this.fileFormat)
                        .fileName(this.fileName)
                        .maxSize(this.maxSize)
                        .batchSize(this.batchSize)
                        .build();
            }
            this.writer.startOutput();
        }
        return false;
    }

    @Override
    public boolean stopOutput() throws OutputHandleException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRunning() throws OutputHandleException {
        // TODO Auto-generated method stub
        return false;
    }

}
