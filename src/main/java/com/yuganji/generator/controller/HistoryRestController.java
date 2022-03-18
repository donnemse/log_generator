package com.yuganji.generator.controller;

import com.yuganji.generator.history.HistoryService;
import com.yuganji.generator.model.SingleObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class HistoryRestController {
    
    @Autowired
    private HistoryService historyService;
    
    @RequestMapping(value = "/history/{page}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list(
            @PathVariable(value = "page") int page) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", historyService.list(page));
    }
}
