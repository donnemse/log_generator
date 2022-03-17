package com.yuganji.generator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yuganji.generator.field.*;
import com.yuganji.generator.util.Constants;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldInfoVO implements Comparable<Integer> {
    private String type;
    private Map<String, Double> values;
    private String rawFormat;
    private String parseFormat;
    private String based;
    private String parserName;

    @JsonIgnore
    private transient int order;

    @JsonIgnore
    private transient IFieldGenerator ins;

    @JsonIgnore
    public IFieldGenerator getInstance() {
        if (type.equalsIgnoreCase(Constants.DataType.IP.getValue())) {
            return new IPField(values);
        } else if (type.equalsIgnoreCase(Constants.DataType.STR.getValue())) {
            return new StrField(values);
        } else if (type.equalsIgnoreCase(Constants.DataType.TIME.getValue())) {
            return new TimeField(rawFormat, parseFormat);
        } else if (type.equalsIgnoreCase(Constants.DataType.INT.getValue())) {
            return new IntField(values);
        } else if (type.equalsIgnoreCase(Constants.DataType.PAYLOAD.getValue())) {
            return new PayloadField(values);
        } else if (type.equalsIgnoreCase(Constants.DataType.URL.getValue())) {
            return new UrlField(values);
        } else if (type.equalsIgnoreCase(Constants.DataType.SPARROW_ID.getValue())) {
            return new IDField(this.parserName);
        }else if (type.equalsIgnoreCase(Constants.DataType.IP2LOC.getValue())) {
            return new Ip2LocField(this.based, 2);
        }
//        throw new Exception(this.toString());
        return null;
    }
    
    public FieldVO get() throws Exception {
        if (this.ins == null) {
            this.ins = this.getInstance();
        }
        if (this.ins == null) {
            throw new Exception("### ERROR ###\n" + this);
        }
        return this.ins.get();
    }

    @Override
    public int compareTo(Integer o) {
        return o.compareTo(order);
    }
}
