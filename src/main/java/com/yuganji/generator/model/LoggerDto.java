package com.yuganji.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yuganji.generator.ApplicationContextProvider;
import com.yuganji.generator.db.Logger;
import com.yuganji.generator.engine.Ip2LocationService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Log4j2
public class LoggerDto {

    @JsonIgnore
    @Builder.Default
    private ObjectMapper om = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private int id;
    private String name;
    private LoggerDetailDto detail;
    private String yamlStr;
    private String ip;

    @Builder.Default
    private long created = System.currentTimeMillis();

    @Builder.Default
    private long lastModified = System.currentTimeMillis();

    @Builder.Default
    private int status = 0;

    public String getIp() {
        if (this.ip == null) {
            return "unknown";
        }
        return this.ip;
    }
    
    public long getLastModified() {
        if (this.lastModified == 0L) {
            return new Date().getTime();
        }
        return this.lastModified;
    }
    public void setYamlStr(String yamlStr) {
        LoggerDetailDto lp = null;
        try {
            lp = om.readValue(yamlStr, LoggerDetailDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        this.detail = lp;
    }
    
    public void setMapCache(MapCache mapCache) {
        this.detail.setMapCache(mapCache);
    }

    @JsonIgnore
    public MapCache getMapCache() {
        return this.detail.getMapCache();
    }

    public Logger toEntity() {
        return Logger.builder(this).build();
    }

    public static class LoggerDtoBuilder {

        private ObjectMapper om = new ObjectMapper(new YAMLFactory())
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        private Ip2LocationService ip2LocService = ApplicationContextProvider.getApplicationContext().getBean(Ip2LocationService .class);

        public LoggerDtoBuilder yamlStr(String yamlStr){
            this.yamlStr = yamlStr;
            LoggerDetailDto lp = null;
            try {
                lp = om.readValue(yamlStr, LoggerDetailDto.class);
                MapCache mapCache = new MapCache();
                mapCache.setIp2Locations(ip2LocService);
                lp.setMapCache(mapCache);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
            this.detail = lp;
            return this;
        }
    }
}
