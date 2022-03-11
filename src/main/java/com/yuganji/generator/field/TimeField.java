package com.yuganji.generator.field;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;

public class TimeField extends FieldInfoVO implements IFieldGenerator {
    
    private transient SimpleDateFormat sdfRaw;
    private transient SimpleDateFormat sdfParsed;
    
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
