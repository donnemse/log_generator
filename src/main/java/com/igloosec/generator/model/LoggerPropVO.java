package com.igloosec.generator.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;

import lombok.Data;

@Data
public class LoggerPropVO {
    private String log;
    private long eps;
    private String logtype;
    private String raw;
    
    private Map<String, FieldInfoVO> data;
    
    
    public void setData(Map<String, FieldInfoVO> data){
        this.data = data;
    }
    
    public Map<String, FieldInfoVO> getData() {
        return data;
    }
    
    public Map<String, Object> generateLog() throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> raw = this.getData().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> {
                    try {
                        FieldVO gen = entry.getValue().get();
                        map.put(entry.getKey(), gen.getValue());
                        return gen.getRawValue();
                    } catch (Exception e) {
                        map.put("_error", e.getCause());
                        map.put("_error_msg", entry.getValue().toString());
                        return e.getMessage();
                    }
                }));
        if (map.containsKey("_error")) {
            throw new Exception(map.get("_error_msg").toString(), (Throwable) map.get("_error"));
        }
        map.put("RAW", StringSubstitutor.replace(this.raw, raw));
        return map;
//        StringSubstitutor subs = new StringSubstitutor();
//        return null;
    }
}
