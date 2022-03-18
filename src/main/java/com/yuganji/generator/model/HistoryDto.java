package com.yuganji.generator.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistoryDto {
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
