package com.igloosec.generator.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.igloosec.generator.prop.LoggerPropertyManager;
import com.igloosec.generator.restful.model.LoggerYamlVO;
import com.igloosec.generator.restful.model.SingleObjectResponse;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping(value = "/logger")
@Log4j2
public class LoggerManageRestController {
    
    @Autowired
    private LoggerPropertyManager loggerPropMng;
    
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "id") int id) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.getLogger(id));
    }
    
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.listLogger());
    }
    
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse create(@RequestBody LoggerYamlVO vo) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.createLogger(vo));
    }
    
    @RequestMapping(value = "/modify", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse modify(
            @RequestBody LoggerYamlVO vo,
            HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        log.info(">>>> X-FORWARDED-FOR : " + ip);
 
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
            log.info(">>>> Proxy-Client-IP : " + ip);
        }
        vo.setIp(ip);
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.modifyLogger(vo));
    }
    
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public @ResponseBody SingleObjectResponse delete(@RequestBody LoggerYamlVO vo) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerPropMng.deleteLogger(vo));
    }
}
