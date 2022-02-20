package com.igloosec.generator.prop;

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
}
