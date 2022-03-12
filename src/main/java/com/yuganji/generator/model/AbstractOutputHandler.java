package com.yuganji.generator.model;

import com.yuganji.generator.util.Constants;

import lombok.Data;

@Data
public abstract class AbstractOutputHandler implements IOutput {
    
//    public AbstractOutputHandler getHandler() {
//        if (this.type.equals(Constants.Output.SPARROW.getValue())) {
//            return new SparrowOutput().getPort()
//        }
//        
//        return handler;
//    }
}
