package com.yuganji.generator.model;

public interface IOutput {
    public boolean startOutput() throws OutputHandleException;
    public boolean stopOutput() throws OutputHandleException;
    public boolean isRunning() throws OutputHandleException;
}
