package com.igloosec.generator.prop;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.igloosec.generator.field.FieldInfo;
import com.igloosec.generator.field.FieldValue;

import lombok.Data;

@Data
public class LoggerProperty {
    private String log;
    private long eps;
    private String logtype;
    private String raw;
    private String yamlStr;
    
    private Map<String, FieldInfo> data;
    
    
    public void setData(Map<String, FieldInfo> data){
        this.data = data;
    }
    
    public Map<String, FieldInfo> getData() {
        return data;
    }
    
    public Map<String, Object> generateLog() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> raw = this.getData().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> {
                    FieldValue gen = entry.getValue().get();
                    map.put(entry.getKey(), gen.getValue());
                    return gen.getRawValue();
                }));
     
        map.put("RAW", StringSubstitutor.replace(this.raw, raw));
        return map;
//        StringSubstitutor subs = new StringSubstitutor();
//        return null;
    }
}
