package com.yuganji.generator.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutputTemplate {
    private String label;
    private Object placeholder;

    private String type;

    private Map<String, List<OutputTemplate>> info;

    public void addSubTemplate(String name, List<OutputTemplate> templates) {
        if (this.info == null) {
            this.info = new HashMap<>();
        }
        this.info.put(name, templates);
    }
}