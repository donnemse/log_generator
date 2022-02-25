package com.igloosec.generator.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.igloosec.generator.field.IFieldGenerator;
import com.igloosec.generator.field.IPField;
import com.igloosec.generator.field.IntField;
import com.igloosec.generator.field.PayloadField;
import com.igloosec.generator.field.StrField;
import com.igloosec.generator.field.TimeField;
import com.igloosec.generator.field.UrlField;
import com.igloosec.generator.util.Constants;

import lombok.Data;

@Data
public class FieldInfoVO {
    private String type;
    private Map<String, Double> values;
    @JsonProperty("raw_format")
    private String rawFormat;
    @JsonProperty("parse_format")
    private String parseFormat;
    private String based;
    
    private IFieldGenerator ins;
    
    public IFieldGenerator getInstance() {
        if (type.equals(Constants.DataType.IP.getValue())) {
            return new IPField(values);
        } else if (type.equals(Constants.DataType.STR.getValue())) {
            return new StrField(values);
        } else if (type.equals(Constants.DataType.TIME.getValue())) {
            return new TimeField(rawFormat, parseFormat);
        } else if (type.equals(Constants.DataType.INT.getValue())) {
            return new IntField(values);
        } else if (type.equals(Constants.DataType.PAYLOAD.getValue())) {
            return new PayloadField(values);
        } else if (type.equals(Constants.DataType.URL.getValue())) {
            return new UrlField(values);
        }
        return null;
    }
    
    public FieldVO get() {
        if (this.ins == null) {
            this.ins = this.getInstance();
        }
        return this.ins.get();
    }
}
