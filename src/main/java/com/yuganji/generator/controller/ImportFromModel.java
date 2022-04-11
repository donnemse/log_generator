package com.yuganji.generator.controller;

import lombok.Data;

@Data
public class ImportFromModel {
    private String zookeeperUrl;
    private String modelId;
    private String ip;
}
