package com.yuganji.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonIgnore
    public MapCache getMapCache() {
        return this.logger.getMapCache();
    }
}
