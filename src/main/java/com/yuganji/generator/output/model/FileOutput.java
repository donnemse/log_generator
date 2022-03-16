package com.yuganji.generator.output.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.output.file.OutputFileWriter;
import com.yuganji.generator.output.file.RawOutputWriter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileOutput extends AbstractOutputHandler {
    @JsonIgnore
    private transient int id;

    @JsonIgnore
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

    @Override
    public boolean isReadyForRunning() throws OutputHandleException {
        return writer.isReadyForRunning();
    }

}
