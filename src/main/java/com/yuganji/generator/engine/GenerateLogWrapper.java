package com.yuganji.generator.engine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import lombok.Data;

@Data
public class GenerateLogWrapper {
    private List<Map<String, Object>> log;
    
    public GenerateLogWrapper(List<Map<String, Object>> log) {
        this.log = log;
    }
    
    public GenerateLogWrapper(Map<String, Object> log) {
        this.log = new ArrayList<>();
        this.log.add(log);
    }
    
    public List<Map<String, Object>> getListofMap() {
        return this.log;
    }
    
    public String getCsvString() {
        CsvWriterSettings settings = new CsvWriterSettings();
        settings.setMaxCharsPerColumn(1024 * 1024 * 10);
        OutputStream os = new ByteArrayOutputStream();
        CsvWriter writer = new CsvWriter(os, settings);
        writer.writeHeadersToString(this.log.get(0).keySet());
        
                
        return null;
    }
//    private String origin;
//    private String originName;
//    private String originId;
//    private String id;
//    private String mgrIp;
//    private String mgrTime;
//    private String eventTime;
//    
//    private String RAW;
//    
//    private String sInfo;
//    private String dInfo;
//    private String sIp;
//    private String dIp;
//    
//    private String sCountry;
//    private String dCountry;
//
//    private String logtype;
//    private String method;
}
