package com.yuganji.generator.output.model;

import com.yuganji.generator.exception.OutputHandleException;

public interface IOutput {
    boolean startOutput() throws OutputHandleException;
    boolean stopOutput() throws OutputHandleException;
    boolean isRunning() throws OutputHandleException;
    boolean isReadyForRunning() throws OutputHandleException;
}
