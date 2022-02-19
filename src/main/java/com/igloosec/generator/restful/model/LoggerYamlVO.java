package com.igloosec.generator.restful.model;

import lombok.Data;

@Data
public class LoggerYamlVO {
    private String yaml;
    private String fileName;
    private String newFileName;
    private String ip;
}
