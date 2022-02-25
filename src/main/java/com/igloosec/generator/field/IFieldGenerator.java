package com.igloosec.generator.field;

import com.igloosec.generator.model.FieldVO;

public interface IFieldGenerator {
    IFieldGenerator getInstance();
    FieldVO get();
}
