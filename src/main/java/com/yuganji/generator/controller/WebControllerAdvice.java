package com.yuganji.generator.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class WebControllerAdvice {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @ModelAttribute("contextPath")
    public String contextPath() {
        return contextPath;
    }
}
