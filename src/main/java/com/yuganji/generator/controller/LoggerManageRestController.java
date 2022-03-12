package com.yuganji.generator.controller;

import javax.servlet.http.HttpServletRequest;

import com.yuganji.generator.util.NetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.yuganji.generator.engine.GeneratorManager;
import com.yuganji.generator.logger.LoggerManager;
import com.yuganji.generator.model.LoggerRequestVO;
import com.yuganji.generator.model.SingleObjectResponse;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping(value = "/api/logger")
@Log4j2
public class LoggerManageRestController {
    @Autowired
    private GeneratorManager genMgr;
    @Autowired
    private LoggerManager loggerPropMng;
    
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "id") int id) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.getLogger(id));
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        Gson gson = new Gson();
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.listLogger());
    }
    
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse create(
            @RequestBody LoggerRequestVO vo,
            HttpServletRequest request) {
        vo.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.createLogger(vo);
    }
    
    @RequestMapping(value = "/modify", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse modify(
            @RequestBody LoggerRequestVO vo,
            HttpServletRequest request) {
        vo.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.modifyLogger(vo);
    }
    
    @RequestMapping(value = "/remove", method = RequestMethod.DELETE)
    public @ResponseBody SingleObjectResponse delete(
            @RequestBody LoggerRequestVO vo,
            HttpServletRequest request) {
        vo.setIp(NetUtil.getClientIP(request));
        return loggerPropMng.removeLogger(vo);
    }
    
    @RequestMapping(value = "/sample", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse sample(@RequestBody LoggerRequestVO vo) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.sample(vo));
    }
    
    @RequestMapping(value = "/start/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse start(
            @PathVariable(value = "id") int id,
            HttpServletRequest request) {
        return genMgr.start(id, NetUtil.getClientIP(request));
    }
    
    @RequestMapping(value = "/stop/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse stop(
            @PathVariable(value = "id") int id,
            HttpServletRequest request) {
        return genMgr.stop(id, NetUtil.getClientIP(request));
    }
}