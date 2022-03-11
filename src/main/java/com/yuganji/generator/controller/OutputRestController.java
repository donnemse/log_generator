package com.yuganji.generator.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.yuganji.generator.model.OutputVO;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.util.NetUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping(value = "/api/output")
public class OutputRestController {

    private OutputService outputService;

    @Autowired
    public OutputRestController(OutputService socketService) {
        this.outputService = socketService;
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", outputService.list());
    }
    
    @RequestMapping(value = "/open", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse open(
            @RequestBody OutputVO vo,
            HttpServletRequest request) {
        vo.setIp(NetUtil.getClientIP(request));
        return outputService.startOutput(vo);
    }
    
    @RequestMapping(value = "/close/{port}", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse close(
            @PathVariable(value = "port") int port,
            HttpServletRequest request) {
        return outputService.stopOutput(port, NetUtil.getClientIP(request));
    }

    @RequestMapping(value = "/close/client/{port}/{clientId}", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse closeClient(
            @PathVariable(value = "port") int port,
            @PathVariable(value = "clientId") String clientId,
            HttpServletRequest request) {
        return outputService.closeClient(port, clientId, NetUtil.getClientIP(request));
    }
    
    @RequestMapping(value = "/get/{port}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "port") int port) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", outputService.get(port));
    }
    
    @RequestMapping(value = "/stop_client/{port}/{clientId}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse stopClient(
            @PathVariable(value = "port") int port,
            @PathVariable(value = "clientId") String clientId,
            HttpServletRequest request) {
        return outputService.closeClient(port, clientId, NetUtil.getClientIP(request));
    }
    
    @RequestMapping(value = {
            "/producer/eps/{port}",
            "/producer/eps/{port}/{loggerId}"
        }, method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse producerEps(
            @PathVariable(value = "port") int port,
            @PathVariable(value = "loggerId", required = false) Integer loggerId) {
        log.debug(port + " " + loggerId);
        
        if (loggerId == null) {
            return new SingleObjectResponse(HttpStatus.OK.value(), "OK", 
                    outputService.listProducerEpsHistory(port));
        }
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", 
                outputService.listProducerEpsHistory(port, loggerId));
    }
}
