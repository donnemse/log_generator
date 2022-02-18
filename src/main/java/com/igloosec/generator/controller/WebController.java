package com.igloosec.generator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.igloosec.generator.restful.model.SingleObjectResponse;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class WebController {
    
    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("test", "test");
        return "main";
    }
    
    
    @RequestMapping(value = "/open_port/{port}", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse main(@PathVariable(value = "port") int port) {
        
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", null);
    }
}
