package com.yuganji.generator.model;

public class OutputHandleException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4454357555214293939L;
    
    public OutputHandleException(String msg) {
        super(msg);
    }
    
    public OutputHandleException(String msg, String reason) {
        super(msg + ((reason == null)
                ? ""
                : " (" + reason + ")"));
    }

    public OutputHandleException(Exception e) {
        super(e);
    }
}
