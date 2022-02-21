package com.igloosec.generator.engine;

public abstract class AGenerator extends Thread {
    abstract boolean startGenerator();
    abstract boolean stopGenerator();
    abstract int checkStatus();
}
