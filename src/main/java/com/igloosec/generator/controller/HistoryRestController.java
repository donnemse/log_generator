package com.igloosec.generator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.igloosec.generator.history.HistoryService;
import com.igloosec.generator.model.SingleObjectResponse;

@RestController
@RequestMapping(value = "/api/history")
public class HistoryRestController {
    
    @Autowired
    private HistoryService historyService;
    
    @RequestMapping(value = "/list/{page}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list(
            @PathVariable(value = "page") int page) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", historyService.list(page));
    }
}
