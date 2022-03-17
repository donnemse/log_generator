package com.yuganji.generator.controller;

import com.yuganji.generator.db.Output;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.util.NetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
            @RequestBody Output output,
            HttpServletRequest request) {
        output.setIp(NetUtil.getClientIP(request));
        return outputService.createOutput(output);
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
        if (loggerId == null) {
            return new SingleObjectResponse(HttpStatus.OK.value(), "OK", 
                    outputService.listProducerEpsHistory(id));
        }
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", 
                outputService.listProducerEpsHistory(id, loggerId));
    }
}
