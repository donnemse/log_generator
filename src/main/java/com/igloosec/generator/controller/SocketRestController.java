package com.igloosec.generator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.igloosec.generator.restful.model.SingleObjectResponse;
import com.igloosec.generator.service.SocketService;

@RestController
@RequestMapping(value = "/api/socket")
public class SocketRestController {
    
    @Autowired
    private SocketService socketService;
    
    @RequestMapping(value = "/open/{port}", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse open(@PathVariable(value = "port") int port) {
        socketService.open(port);
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", null);
    }
    
    @RequestMapping(value = "/close/{port}", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse close(@PathVariable(value = "port") int port) {
        socketService.close(port);
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", null);
    }
}
