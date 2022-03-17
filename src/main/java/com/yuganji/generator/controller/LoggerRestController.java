package com.yuganji.generator.controller;

import com.google.gson.Gson;
import com.yuganji.generator.db.Logger;
import com.yuganji.generator.engine.GeneratorManager;
import com.yuganji.generator.logger.LoggerService;
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
    private LoggerService loggerPropMng;
    
    @RequestMapping(value = "/loggers/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "id") int id) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.get(id));
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        Gson gson = new Gson();
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.list());
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse add(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.add(logger);
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse modify(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.modify(logger);
    }
    
    @RequestMapping(value = "/loggers", method = RequestMethod.DELETE)
    public @ResponseBody SingleObjectResponse remove(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.remove(logger);
    }
    
    @RequestMapping(value = "/loggers/sample", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse sample(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.sample(logger));
    }
    
    @RequestMapping(value = "/loggers/start", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse start(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return genMgr.start(logger);
    }
    
    @RequestMapping(value = "/loggers/stop", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse stop(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return genMgr.stop(logger);
    }
}
