package com.yuganji.generator.model;

import lombok.Data;

@Data
public class LoggerRequestVO {
    private int id;
    private String yaml;
    private String name;
    private String ip;
}
