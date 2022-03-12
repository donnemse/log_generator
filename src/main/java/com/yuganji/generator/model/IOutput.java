package com.yuganji.generator.model;

import com.yuganji.generator.exception.OutputHandleException;

public interface IOutput {
    public boolean startOutput() throws OutputHandleException;
    public boolean stopOutput() throws OutputHandleException;
    public boolean isRunning() throws OutputHandleException;
}
