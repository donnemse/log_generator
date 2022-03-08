package com.igloosec.generator.field;

import com.igloosec.generator.model.FieldInfoVO;
import com.igloosec.generator.model.FieldVO;

public class Ip2LocField extends FieldInfoVO implements IFieldGenerator {
    private String based;
    
    public Ip2LocField(String based) {
        this.based = based;
    }

    @Override
    public FieldVO get() {
        return new FieldVO(based, based);
    }

}
