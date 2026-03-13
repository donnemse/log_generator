package com.yuganji.generator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping(value={"/", "/logger"})
    public String logger(Model model) {
        model.addAttribute("logger", true);
        return "logger";
    }
    
    @GetMapping(value={"/hist"})
    public String main(Model model) {
        model.addAttribute("hist", true);
        return "hist";
    }
}
