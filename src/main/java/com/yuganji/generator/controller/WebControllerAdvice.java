package com.yuganji.generator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
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

    // absent when build-info.properties wasn't generated (e.g. non-Gradle IDE build)
    @Autowired(required = false)
    private BuildProperties buildProperties;

    @ModelAttribute("appVersion")
    public String appVersion() {
        return buildProperties != null ? buildProperties.getVersion() : "dev";
    }
}
