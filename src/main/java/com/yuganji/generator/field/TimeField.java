package com.yuganji.generator.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeField extends FieldInfoVO implements IFieldGenerator {

    @JsonIgnore
    private transient final DateTimeFormatter rawFormatter;
    @JsonIgnore
    private transient final DateTimeFormatter parsedFormatter;

    public TimeField(String rawFormat, String parseFormat) {
        this.rawFormatter = DateTimeFormatter.ofPattern(rawFormat);
        this.parsedFormatter = DateTimeFormatter.ofPattern(parseFormat);
    }

    @Override
    public FieldVO get() {
        ZonedDateTime now = ZonedDateTime.now();
        return new FieldVO(
                now.format(rawFormatter),
                now.format(parsedFormatter));
    }
}
