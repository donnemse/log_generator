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
    @JsonIgnore
    private transient volatile String[] fieldKeys;
    @JsonIgnore
    private transient Map<String, Integer> fieldKeyIndex;
    @JsonIgnore
    private transient int[] templateKeyIndices;

    
    public void setData(Map<String, FieldInfoVO> data){
        this.data = data;
        Comparator<Map.Entry<String, FieldInfoVO>> order = Entry.comparingByValue(Comparator.comparing(FieldInfoVO::getOrder));
        this.data = data.entrySet().stream()
                .sorted(order)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        // Eagerly initialize all field generators for thread-safety
        for (FieldInfoVO field : this.data.values()) {
            try {
                field.initializeGenerator();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize field generator: " + field.getType(), e);
            }
        }
    }
    
    public Map<String, FieldInfoVO> getData() {
        return data;
    }

    private void compileTemplate() {
        if (this.raw == null) {
            this.templateSegments = new String[0];
            this.templateKeys = new String[0];
        } else {
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

        // Build shared key arrays for LogEvent (MUST always execute)
        if (this.data != null) {
            this.fieldKeys = new String[this.data.size() + 1]; // +1 for RAW
            this.fieldKeyIndex = new HashMap<>(this.data.size() + 1, 1.0f);
            int idx = 0;
            for (String key : this.data.keySet()) {
                this.fieldKeys[idx] = key;
                this.fieldKeyIndex.put(key, idx);
                idx++;
            }
            // RAW field at the end
            this.fieldKeys[idx] = "raw";
            this.fieldKeyIndex.put("raw", idx);

            // Pre-compute template key indices for raw value lookup
            this.templateKeyIndices = new int[this.templateKeys.length];
            for (int i = 0; i < this.templateKeys.length; i++) {
                Integer keyIdx = this.fieldKeyIndex.get(this.templateKeys[i]);
                this.templateKeyIndices[i] = keyIdx != null ? keyIdx : -1;
            }
        }
    }

    public Map<String, Object> generateLog() throws Exception {
        if (this.fieldKeys == null) {
            synchronized (this) {
                if (this.fieldKeys == null) {
                    compileTemplate();
                }
            }
        }
        LogEvent event = new LogEvent(this.fieldKeys, this.fieldKeyIndex);
        Object[] rawValues = new Object[this.fieldKeys.length];

        int idx = 0;
        for (Entry<String, FieldInfoVO> entry : this.getData().entrySet()) {
            try {
                FieldVO gen = entry.getValue().get();
                if (entry.getValue().getType().equals(Constants.DataType.IP2LOC.getValue())) {
                    Integer basedIdx = this.fieldKeyIndex.get(entry.getValue().getBased());
                    String basedVal = basedIdx != null ? String.valueOf(event.get(entry.getValue().getBased())) : "";
                    String val = mapCache.getIp2Locations().getLocation(basedVal).getCode();
                    gen = new FieldVO(val, val);
                }
                event.putByIndex(idx, gen.getValue());
                rawValues[idx] = gen.getRawValue();
            } catch (Exception e) {
                throw e;
            }
            idx++;
        }

        // Template substitution using pre-computed indices
        StringBuilder rawStr = new StringBuilder(128);
        for (int i = 0; i < templateKeys.length; i++) {
            rawStr.append(templateSegments[i]);
            int keyIdx = templateKeyIndices[i];
            if (keyIdx >= 0 && rawValues[keyIdx] != null) {
                rawStr.append(rawValues[keyIdx]);
            }
        }
        if (templateSegments.length > templateKeys.length) {
            rawStr.append(templateSegments[templateSegments.length - 1]);
        }
        // RAW is the last field in fieldKeys
        event.putByIndex(this.fieldKeys.length - 1, rawStr.toString());
        return event;
    }
}
