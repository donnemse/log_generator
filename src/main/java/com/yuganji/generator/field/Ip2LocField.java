package com.yuganji.generator.field;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;

public class Ip2LocField extends FieldInfoVO implements IFieldGenerator {
    private String based;
    
    public Ip2LocField(String based, int order) {
        this.based = based;
        super.setOrder(order);
    }

    @Override
    public FieldVO get() {
        return new FieldVO(based, based);
    }

}
