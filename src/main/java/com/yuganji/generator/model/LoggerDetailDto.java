package com.yuganji.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yuganji.generator.util.Constants;
import lombok.Data;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

    @JsonIgnore
    private transient String[] templateSegments;
    @JsonIgnore
    private transient String[] templateKeys;
    
    
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

    private void compileTemplate() {
        if (this.raw == null) {
            this.templateSegments = new String[0];
            this.templateKeys = new String[0];
            return;
        }
        List<String> segments = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        int pos = 0;
        while (pos < raw.length()) {
            int start = raw.indexOf("${", pos);
            if (start == -1) {
                segments.add(raw.substring(pos));
                break;
            }
            segments.add(raw.substring(pos, start));
            int end = raw.indexOf("}", start + 2);
            if (end == -1) {
                segments.add(raw.substring(start));
                break;
            }
            keys.add(raw.substring(start + 2, end));
            pos = end + 1;
        }
        this.templateSegments = segments.toArray(new String[0]);
        this.templateKeys = keys.toArray(new String[0]);
    }

    public Map<String, Object> generateLog() throws Exception {
        if (this.templateSegments == null) {
            compileTemplate();
        }
        int size = this.getData().size();
        Map<String, Object> map = new HashMap<>(size + 1, 1.0f);
        Map<String, Object> raw = new HashMap<>(size, 1.0f);
        for (Entry<String, FieldInfoVO> entry : this.getData().entrySet()) {
            FieldVO gen = entry.getValue().get();
            if (entry.getValue().getType().equals(Constants.DataType.IP2LOC.getValue())) {
                String val = mapCache.getIp2Locations().getLocation(String.valueOf(map.get(entry.getValue().getBased()))).getCode();
                gen = new FieldVO(val, val);
            }
            map.put(entry.getKey(), gen.getValue());
            raw.put(entry.getKey(), gen.getRawValue());
        }
        StringBuilder rawStr = new StringBuilder(128);
        for (int i = 0; i < templateKeys.length; i++) {
            rawStr.append(templateSegments[i]);
            Object val = raw.get(templateKeys[i]);
            if (val != null) {
                rawStr.append(val);
            }
        }
        if (templateSegments.length > templateKeys.length) {
            rawStr.append(templateSegments[templateSegments.length - 1]);
        }
        map.put("RAW", rawStr.toString());
        return map;
    }
}
