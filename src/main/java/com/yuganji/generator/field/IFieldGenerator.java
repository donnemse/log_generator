package com.yuganji.generator.field;

import com.yuganji.generator.model.FieldVO;

public interface IFieldGenerator {
    IFieldGenerator getInstance();
    FieldVO get();
}
