package com.yuganji.generator.output.file;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper=false)
@Log4j2
public abstract class OutputFileWriter extends Thread implements IFileWriter {
    protected Map<String, Object> writers = new ConcurrentHashMap<>();

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
        String fileName = file.getName();
        if (this.writers.get(fileName) == null){
            this.writers.keySet().stream()
                    .filter(x -> x.startsWith(prefix))
                    .forEach(x -> {
                CsvWriter w = this.getWriter(x, CsvWriter.class);
                w.flush();
                w.flush();
                w.close();
                this.writers.remove(x);
            });
            CsvWriterSettings settings = new CsvWriterSettings();
            settings.setHeaders(row.keySet().stream().toArray(String[]::new));
            writers.put(fileName, new CsvWriter(file, settings));
        }
        CsvWriter w = this.getWriter(fileName, CsvWriter.class);
        long cnt = w.getRecordCount();
        if (cnt == 0) {
            w.writeHeaders();
        } else if (cnt % 100 == 0) {
            w.flush();
        }
        w.writeRow(row);
    }

    private <T extends Object> T getWriter(String filename, Class<T> c) {
        return c.cast(this.writers.get(filename));
    }

}
