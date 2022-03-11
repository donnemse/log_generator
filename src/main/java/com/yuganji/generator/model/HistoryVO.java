package com.yuganji.generator.model;

import lombok.Data;

@Data
public class HistoryVO {
    private int id;
    private String ip;
    private String name;
    private int fid;
    private String type;
    private long lastModified;
    private String msg;
    private String detail;
    private String error;
    private String last;
    
}
