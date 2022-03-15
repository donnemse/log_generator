package com.yuganji.generator.output.model;

import com.yuganji.generator.exception.OutputHandleException;

import com.yuganji.generator.model.AbstractOutputHandler;
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
    private transient final String KEY_FILE_PREFIX = "file_prefix";
    private transient final String KEY_MAX_SIZE = "max_size";
    private transient final String KEY_BATCH_SIZE = "batch_size";


    private transient int id;

    private transient OutputFileWriter writer;

    public FileOutput(int id, Map<String, Object> conf) {
        this.id = id;
        this.writer = new RawOutputWriter(id, conf);
    }

    @Override
    public boolean startOutput() throws OutputHandleException {
        return this.writer.startOutput();
    }

    @Override
    public boolean stopOutput() throws OutputHandleException {
        return this.writer.stopOutput();
    }

    @Override
    public boolean isRunning() throws OutputHandleException {
        return this.writer.isRunning();
    }

}
