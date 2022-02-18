package com.igloosec.generator.field;

import lombok.Data;

@Data
public class FieldValue {
    private Object rawValue;
    private Object value;
    
    public FieldValue(Object rawValue, Object value) {
        this.rawValue = rawValue;
        this.value = value;
    }
}
