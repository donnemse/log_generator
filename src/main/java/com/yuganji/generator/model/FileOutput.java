package com.yuganji.generator.model;

import com.yuganji.generator.exception.OutputHandleException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileOutput extends AbstractOutputHandler {
    
    private String path;
    private String outputType;
    private String rotationPolicy;
    private String fileFormat;
    private String fileName;
    private int maxSize;
    
    @Override
    public boolean startOutput() throws OutputHandleException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stopOutput() throws OutputHandleException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRunning() throws OutputHandleException {
        // TODO Auto-generated method stub
        return false;
    }

}
