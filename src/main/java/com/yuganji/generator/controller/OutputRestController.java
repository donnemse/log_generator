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

import com.yuganji.generator.output.model.Output;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.util.NetUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping(value = "/api")
public class OutputRestController {

    private OutputService outputService;

    @Autowired
    public OutputRestController(OutputService socketService) {
        this.outputService = socketService;
    }
    
    @RequestMapping(value = "/outputs", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", outputService.list());
    }

    @RequestMapping(value = "/outputs/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "id") int id) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", outputService.get(id));
    }

    @RequestMapping(value = "/outputs", method = RequestMethod.PUT)
    public @ResponseBody SingleObjectResponse create(
            @RequestBody Output vo,
            HttpServletRequest request) {
        vo.setIp(NetUtil.getClientIP(request));
        return outputService.createOutput(vo);
    }

    @RequestMapping(value = "/outputs", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse modify(
            @RequestBody Output vo,
            HttpServletRequest request) {
        vo.setIp(NetUtil.getClientIP(request));
        return outputService.modifyOutput(vo);
    }

    @RequestMapping(value = "/outputs", method = RequestMethod.DELETE)
    public @ResponseBody SingleObjectResponse delete(
            @RequestBody Output vo,
            HttpServletRequest request) {
        vo.setIp(NetUtil.getClientIP(request));
        return outputService.removeLogger(vo);
    }

    @RequestMapping(value = "/outputs/start/{id}", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse start(
            @PathVariable(value = "id") int id,
            HttpServletRequest request) {
        return outputService.startOutput(id, NetUtil.getClientIP(request));
    }
    
    @RequestMapping(value = "/outputs/stop/{id}", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse stop(
            @PathVariable(value = "id") int id,
            HttpServletRequest request) {
        return outputService.stopOutput(id, NetUtil.getClientIP(request));
    }

    @RequestMapping(value = "/outputs/stop-client/{id}/{clientId}", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse stopClient(
            @PathVariable(value = "id") int id,
            @PathVariable(value = "clientId") String clientId,
            HttpServletRequest request) {
        return outputService.closeClient(id, clientId, NetUtil.getClientIP(request));
    }
    
    @RequestMapping(value = {
            "/outputs/eps/producer/{id}",
            "/outputs/eps/producer/{id}/{loggerId}"
        }, method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse producerEps(
            @PathVariable(value = "id") int id,
            @PathVariable(value = "loggerId", required = false) Integer loggerId) {
        log.debug(id + " " + loggerId);
        if (loggerId == null) {
            return new SingleObjectResponse(HttpStatus.OK.value(), "OK", 
                    outputService.listProducerEpsHistory(id));
        }
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", 
                outputService.listProducerEpsHistory(id, loggerId));
    }
}
