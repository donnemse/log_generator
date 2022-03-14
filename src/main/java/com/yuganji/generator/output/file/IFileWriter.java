package com.yuganji.generator.output.file;

public interface IFileWriter {
    boolean startOutput();
    boolean stopOutput();
    boolean isRunning();
}
