package com.igloosec.generator.field;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.igloosec.generator.model.FieldInfoVO;
import com.igloosec.generator.model.FieldVO;

public class IDField extends FieldInfoVO implements IFieldGenerator {
    private String parserName;
    private SimpleDateFormat yyyyMMddHH;
    private SimpleDateFormat yyyyMMddHHmmssSSS;
    
    private String currentTime;
    private long count;
    
    public IDField(String parserName) {
        if (parserName == null) {
            this.parserName = "Parser-Name";
        } else {
            this.parserName = parserName;
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
        StringBuffer sb = new StringBuffer();
        
        sb.append(yyyyMMddHHmmssSSS.format(d));
        sb.append('_');
        sb.append(StringUtils.leftPad("" + count++, 20, "0"));
        sb.append('_');
        sb.append(hour);
        sb.append(".log_");
        sb.append(this.parserName);
//        20220103085430265_0000000000012412104_2022010308.log_WEBINSIGHT-V4-500-34808
        
        return new FieldVO(sb.toString(), sb.toString());
    }

}
