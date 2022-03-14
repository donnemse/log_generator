package com.yuganji.generator.output.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

@Data
@EqualsAndHashCode(callSuper=false)
@Log4j2
public abstract class OutputFileWriter extends Thread implements IFileWriter {
    protected Map<String, FileWriterObject> writers = new ConcurrentHashMap<>();
    private Gson gson = new Gson();
    private int cnt;
    
    @Override
    public boolean startOutput() {
        super.start();
        return false;
    }

    @Override
    public boolean stopOutput() {
        super.interrupt();
        return false;
    }

    @Override
    public boolean isRunning() {
        return super.isAlive();
    }

    protected void writeCsv(String prefix, File file, Map<String, Object> row) {
        file = this.uniqueFileName(file);
        String fileName = file.getName();
        if (this.writers.get(fileName) == null){
            this.writers.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .forEach(entry -> {
                CsvWriter w = entry.getValue().getWriter();
                w.flush();
                w.flush();
                w.close();
                this.writers.remove(entry.getKey());
            });
            CsvWriterSettings settings = new CsvWriterSettings();
            writers.put(fileName, new FileWriterObject(new CsvWriter(file, settings)));
        }
        this.writers.get(fileName).write(row);
    }
    
    
    protected void write(String prefix, File file, String raw) {
        this.write(prefix, file, null, raw);
    }
    
    protected void write(String prefix, File file, Map<String, Object> row) {
        this.write(prefix, file, row, null);
    }
    
    private void write(String prefix, File file, Map<String, Object> row, String raw) {
        file = this.uniqueFileName(file);
        String fileName = file.getName();
        
        if (this.writers.get(fileName) == null){
            this.writers.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .forEach(entry -> {
                        try {
                            FileWriter w = entry.getValue().getWriter();
                            w.flush();
                            w.flush();
                            IOUtils.closeQuietly(w);
                            this.writers.remove(entry.getKey());
                        } catch(IOException e) {
                            log.error(e.getMessage(), e);
                        }
            });
            try {
                this.writers.put(fileName, new FileWriterObject(new FileWriter(file)));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        try {
            
            if (row != null) {
                this.writers.get(fileName).write(gson.toJson(row));
            } else {
                this.writers.get(fileName).write(raw);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    private File uniqueFileName(File f) {
        if (!f.exists()) {
            return f;
        }
        int idx = 1;
        while (f.exists()) {
            
            f = new File(f.getParentFile(), 
                    FilenameUtils.getBaseName(f.getName()) + "_" + (idx++)
                    + "." + FilenameUtils.getExtension(f.getName()));
        }
        return f;
    }
}
