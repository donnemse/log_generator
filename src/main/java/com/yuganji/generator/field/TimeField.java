package com.yuganji.generator.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeField extends FieldInfoVO implements IFieldGenerator {

    @JsonIgnore
    private transient final SimpleDateFormat sdfRaw;
    @JsonIgnore
    private transient final SimpleDateFormat sdfParsed;
    
    public TimeField(String rawFormat, String parseFormat) {
        this.sdfRaw = new SimpleDateFormat(rawFormat);
        this.sdfParsed = new SimpleDateFormat(parseFormat);
    }

    @Override
    public FieldVO get() {
        Date d = new Date();
        return new FieldVO(
                sdfRaw.format(d),
                sdfParsed.format(d));
    }
}
