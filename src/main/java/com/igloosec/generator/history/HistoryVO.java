package com.igloosec.generator.history;

import lombok.Data;

@Data
public class HistoryVO {
    private int id;
    private String ip;
    private String name;
    private int loggerId;
    private int outputId;
    private String type;
    private long lastModified;
    private String msg;
    private String etc;
    
}
