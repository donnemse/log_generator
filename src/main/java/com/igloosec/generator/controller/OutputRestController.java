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
import com.igloosec.generator.service.output.OutputInfoVO;
import com.igloosec.generator.service.output.OutputService;

@RestController
@RequestMapping(value = "/api/output")
public class OutputRestController {
    
    @Autowired
    private OutputService socketService;
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", socketService.list());
    }
    
    @RequestMapping(value = "/open", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse open(@RequestBody OutputInfoVO vo) {
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
