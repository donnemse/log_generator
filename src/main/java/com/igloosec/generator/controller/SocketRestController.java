package com.igloosec.generator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.igloosec.generator.restful.model.SingleObjectResponse;
import com.igloosec.generator.service.socket.SocketInfoVO;
import com.igloosec.generator.service.socket.SocketService;

@RestController
@RequestMapping(value = "/api/output")
public class SocketRestController {
    
    @Autowired
    private SocketService socketService;
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", socketService.list());
    }
    
    @RequestMapping(value = "/open", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse open(@RequestBody SocketInfoVO vo) {
        return socketService.open(vo);
    }
    
    @RequestMapping(value = "/close/{port}", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse close(@PathVariable(value = "port") int port) {
        return socketService.close(port);
    }
    
    @RequestMapping(value = "/get/{port}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "port") int port) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", socketService.get(port));
    }
}
