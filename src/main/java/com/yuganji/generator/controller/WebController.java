package com.yuganji.generator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping(value = {"/", "/logger", "/output", "/history"})
    public String forward() {
        return "forward:/index.html";
    }
}
