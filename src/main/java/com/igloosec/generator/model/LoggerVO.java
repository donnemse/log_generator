package com.igloosec.generator.model;

import java.util.Date;

import lombok.Data;

@Data
public class LoggerVO {
    private int id;
    private String name;
    private LoggerPropVO logger;
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
    
    public void setMapCache(MapCache mapCache) {
        this.logger.setMapCache(mapCache);
    }
    
    public MapCache getMapCache() {
        return this.logger.getMapCache();
    }
    
//    public String getLastModifiedStr() {
//        return sdf.format(this.lastModified);
//    }
//    
//    public String getCreatedStr() {
//        return sdf.format(this.created);
//    }
}
