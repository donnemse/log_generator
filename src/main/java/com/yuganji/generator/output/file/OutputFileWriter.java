package com.yuganji.generator.output.file;

import com.google.gson.Gson;
import com.yuganji.generator.output.model.FileOutputConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper=false)
@Log4j2
public abstract class OutputFileWriter extends Thread implements IFileWriter {
    protected Map<String, FileWriterObject> writers = new ConcurrentHashMap<>();
    private Gson gson = new Gson();

    protected FileOutputConfig config;
    protected int outputId;

    @Override
    public boolean startOutput() {
        super.setName("thread_output_" + outputId);
        super.start();
        return true;
    }

    @Override
    public boolean stopOutput() {
        super.interrupt();
        this.writers.forEach((key, value) -> value.close());
        return true;
    }

    @Override
    public boolean isRunning() {
        return super.isAlive();
    }

    protected void write(String prefix, String raw) {
        this.write(prefix, null, raw);
    }
    
    protected void write(String prefix, Map<String, Object> row) {
        this.write(prefix, row, null);
    }

    private void write(String prefix, Map<String, Object> row, String raw) {
        if (this.writers.get(prefix) == null) {
            this.writers.put(prefix, new FileWriterObject(this.config, prefix));
        }
        try {
            if (row != null) {
                this.writers.get(prefix).write(row);
            } else {
                this.writers.get(prefix).write(raw);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
