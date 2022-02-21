package com.igloosec.generator.prop;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class LoggerPropertyInfo {
    private int id;
    private String name;
    private LoggerProperty logger;
    private String yamlStr;
    private String ip;
    private long created;
    private long lastModified;
    private int status;
    private String lastModifiedStr;
    private String createdStr;
    
    private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    private String getIp() {
        if (this.ip == null) {
            return "unknown";
        }
        return this.ip;
    }
    
    private long getLastModified() {
        if (this.ip == null) {
            return new Date().getTime();
        }
        return this.lastModified;
    }
    
    
//    public String getLastModifiedStr() {
//        return sdf.format(this.lastModified);
//    }
//    
//    public String getCreatedStr() {
//        return sdf.format(this.created);
//    }
}
