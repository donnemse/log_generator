package com.yuganji.generator.field;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class IDField extends FieldInfoVO implements IFieldGenerator {
    private String suffix;
    private SimpleDateFormat yyyyMMddHH;
    private SimpleDateFormat yyyyMMddHHmmssSSS;
    
    private String currentTime;
    private long count;
    
    public IDField(String parserName) {
        if (parserName == null) {
            this.suffix = "Parser-Name";
        } else {
            this.suffix = parserName;
        }
        
        this.yyyyMMddHH = new SimpleDateFormat("yyyyMMddHH");
        this.yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        this.currentTime = this.yyyyMMddHH.format(new Date());
    }

    @Override
    public FieldVO get() {
        Date d = new Date();
        String hour = yyyyMMddHH.format(d);
        if (!hour.equals(this.currentTime)) {
            this.count = 0L;
            this.currentTime = hour;
        }
        StringBuilder sb = new StringBuilder();
        
        sb.append(yyyyMMddHHmmssSSS.format(d));
        sb.append('_');
        sb.append(StringUtils.leftPad("" + count++, 20, "0"));
        sb.append('_');
        sb.append(hour);
        sb.append(".log_");
        sb.append(this.suffix);
//        20220103085430265_0000000000012412104_2022010308.log_WEBINSIGHT-V4-500-34808
        
        return new FieldVO(sb.toString(), sb.toString());
    }

}
