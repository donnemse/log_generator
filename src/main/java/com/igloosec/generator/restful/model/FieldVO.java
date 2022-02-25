package com.igloosec.generator.restful.model;

import lombok.Data;

@Data
public class FieldVO {
    private Object rawValue;
    private Object value;
    
    public FieldVO(Object rawValue, Object value) {
        this.rawValue = rawValue;
        this.value = value;
    }
}
