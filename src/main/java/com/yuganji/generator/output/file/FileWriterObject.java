package com.yuganji.generator.output.file;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.univocity.parsers.csv.CsvWriter;

import lombok.Getter;


public class FileWriterObject {
    private CsvWriter csvWriter;
    private FileWriter fileWriter;
//    private Object writer;
    
    @Getter
    private long length;
    
    @Getter
    private long cnt;
    
    public FileWriterObject(CsvWriter writer) {
        this.csvWriter = writer;
    }
    
    public FileWriterObject(FileWriter writer) {
        this.fileWriter = writer;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Object> T getWriter() {
        if (csvWriter == null) {
            return (T) fileWriter;
        } else {
            return (T) csvWriter;
        }
//        return c.cast(this.writer);
    }

    public void write(String raw) throws IOException {
        if (fileWriter == null) {
            return;
        }
        cnt++;
        length += raw.length() + 1;
        fileWriter.write(raw);
        fileWriter.write('\n');
        
        if (this.getCnt() % 100 == 0) {
            fileWriter.flush();
        }
    }

    public void write(Map<String, Object> row) {
        if (csvWriter == null) {
            return;
        }
        String r = csvWriter.writeRowToString(row);
        if (cnt == 0) {
            String h = csvWriter.writeHeadersToString(row.keySet());
            length += h.length() + 1;
            csvWriter.writeHeaders(row.keySet());
        } else if (cnt % 100 == 0) {
            csvWriter.flush();
        }
        cnt++;
        length += r.length() + 1;
    }
}
