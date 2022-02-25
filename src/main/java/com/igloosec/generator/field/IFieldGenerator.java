package com.igloosec.generator.field;

import com.igloosec.generator.restful.model.FieldVO;

public interface IFieldGenerator {
    IFieldGenerator getInstance();
    FieldVO get();
}
