package com.yuganji.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yuganji.generator.util.Constants;
import lombok.Data;
import org.apache.commons.text.StringSubstitutor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoggerDetailDto {
    private String log;
    private String logtype;
    private String raw;

    @JsonIgnore
    private transient MapCache mapCache;
    
    private Map<String, FieldInfoVO> data;
    
    
    public void setData(Map<String, FieldInfoVO> data){
        this.data = data;
        Comparator<Map.Entry<String, FieldInfoVO>> order = Entry.comparingByValue(Comparator.comparing(FieldInfoVO::getOrder));
        this.data = data.entrySet().stream()
                .sorted(order)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }
    
    public Map<String, FieldInfoVO> getData() {
        return data;
    }
    
    public Map<String, Object> generateLog() throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> raw = new HashMap<>();
        for (Entry<String, FieldInfoVO> entry : this.getData().entrySet()) {
            FieldVO gen = entry.getValue().get();
            if (entry.getValue().getType().equals(Constants.DataType.IP2LOC.getValue())) {
                String val = mapCache.getIp2Locations().getLocation(map.get(entry.getValue().getBased()) + "").getCode();
                gen = new FieldVO(val, val);
            }
            map.put(entry.getKey(), gen.getValue());
            raw.put(entry.getKey(), gen.getRawValue());
        }
        map.put("RAW", StringSubstitutor.replace(this.raw, raw));
        return map;
    }
}
