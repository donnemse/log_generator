package com.igloosec.generator.field;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeField extends FieldInfo implements IFieldGenerator {
    
    private SimpleDateFormat sdfRaw;
    private SimpleDateFormat sdfParsed;
    
    public TimeField(String rawFormat, String parseFormat) {
        this.sdfRaw = new SimpleDateFormat(rawFormat);
        this.sdfParsed = new SimpleDateFormat(parseFormat);
    }

    @Override
    public FieldValue get() {
        Date d = new Date();
        return new FieldValue(
                sdfRaw.format(d),
                sdfParsed.format(d));
    }
}
