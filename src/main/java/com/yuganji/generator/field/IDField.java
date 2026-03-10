package com.yuganji.generator.field;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class IDField extends FieldInfoVO implements IFieldGenerator {
    private String suffix;
    private static final DateTimeFormatter FORMATTER_HOUR = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter FORMATTER_MILLIS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private volatile String currentTime;
    private final AtomicLong count = new AtomicLong(0);

    public IDField(String parserName) {
        if (parserName == null) {
            this.suffix = "Parser-Name";
        } else {
            this.suffix = parserName;
        }

        this.currentTime = LocalDateTime.now().format(FORMATTER_HOUR);
    }

    @Override
    public synchronized FieldVO get() {
        LocalDateTime now = LocalDateTime.now();
        String hour = now.format(FORMATTER_HOUR);
        if (!hour.equals(this.currentTime)) {
            this.count.set(0L);
            this.currentTime = hour;
        }
        StringBuilder sb = new StringBuilder();

        sb.append(now.format(FORMATTER_MILLIS));
        sb.append('_');
        sb.append(StringUtils.leftPad(String.valueOf(count.getAndIncrement()), 20, "0"));
        sb.append('_');
        sb.append(hour);
        sb.append(".log_");
        sb.append(this.suffix);

        return new FieldVO(sb.toString(), sb.toString());
    }
}
