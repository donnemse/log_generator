package com.yuganji.generator.controller;

import com.yuganji.generator.db.Logger;
import com.yuganji.generator.engine.GeneratorSerivce;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.util.NetUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = {"Provides API for Logger's CRUD and controls"})
@RestController
@RequestMapping(value = "/api")
public class LoggerRestController {

    @Autowired
    private GeneratorSerivce generatorSerivce;
    @Autowired
    private LoggerService loggerService;

    @ApiOperation(value = "Getting information of Logger")
    @RequestMapping(value = "/loggers", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerService.list());
    }

    @ApiOperation(value = "Getting information of Logger")
    @ApiImplicitParam(name = "id", value = "Look up target ID", required = true, dataType = "int", example = "0")
    @RequestMapping(value = "/loggers/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "id") int id) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerService.get(id));
    }

    @ApiOperation(value = "Add Logger")
    @ApiImplicitParam(name = "logger", value = "Logger details.", required = true, dataTypeClass = Logger.class)
    @RequestMapping(value = "/loggers", method = RequestMethod.PUT)
    public @ResponseBody SingleObjectResponse add(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerService.add(logger);
    }

    @ApiOperation(value = "Modify Logger")
    @ApiImplicitParam(name = "logger", value = "Logger details.", required = true, dataTypeClass = Logger.class)
    @RequestMapping(value = "/loggers", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse modify(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerService.modify(logger);
    }

    @ApiOperation(value = "Remove Logger")
    @ApiImplicitParam(name = "logger", value = "Logger details.", required = true, dataTypeClass = Logger.class)
    @RequestMapping(value = "/loggers", method = RequestMethod.DELETE)
    public @ResponseBody SingleObjectResponse remove(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return loggerService.remove(logger);
    }

    @ApiOperation(value = "Sample Logger")
    @ApiImplicitParam(name = "logger", value = "Logger details. (only Yaml str)", required = true, dataTypeClass = Logger.class)
    @RequestMapping(value = "/loggers/sample", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse sample(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", loggerService.sample(logger));
    }

    @ApiOperation(value = "Start Logger")
    @ApiImplicitParam(name = "logger", value = "Logger details. (only id)", required = true, dataTypeClass = Logger.class)
    @RequestMapping(value = "/loggers/start", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse start(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return generatorSerivce.start(logger);
    }

    @ApiOperation(value = "Stop Logger")
    @ApiImplicitParam(name = "logger", value = "Logger details. (only id)", required = true, dataTypeClass = Logger.class)
    @RequestMapping(value = "/loggers/stop", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse stop(
            @RequestBody Logger logger,
            HttpServletRequest request) {
        logger.setIp(NetUtil.getClientIP(request));
        return generatorSerivce.stop(logger);
    }
}
