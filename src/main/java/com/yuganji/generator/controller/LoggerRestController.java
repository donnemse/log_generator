package com.yuganji.generator.controller;

import com.google.gson.Gson;
import com.yuganji.generator.db.Logger;
import com.yuganji.generator.engine.GeneratorManager;
import com.yuganji.generator.logger.LoggerManager;
import com.yuganji.generator.model.LoggerRequestVO;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.util.NetUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api")
@Log4j2
public class LoggerRestController {
    @Autowired
    private GeneratorManager genMgr;
    @Autowired
    private LoggerManager loggerPropMng;
    
    @RequestMapping(value = "/loggers/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "id") int id) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.getLogger(id));
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        Gson gson = new Gson();
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.listLogger());
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse create(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.createLogger(logger);
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse modify(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.modifyLogger(logger);
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.DELETE)
    public @ResponseBody SingleObjectResponse delete(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.removeLogger(logger);
    }
    
    @RequestMapping(value = "/loggers/sample", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse sample(@RequestBody LoggerRequestVO vo) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.sample(vo));
    }
    
    @RequestMapping(value = "/loggers/start/{id}", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse start(
            @PathVariable(value = "id") int id,
            HttpServletRequest request) {
        return genMgr.start(id, NetUtil.getClientIP(request));
    }
    
    @RequestMapping(value = "/loggers/stop/{id}", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse stop(
            @PathVariable(value = "id") int id,
            HttpServletRequest request) {
        return genMgr.stop(id, NetUtil.getClientIP(request));
    }
}
