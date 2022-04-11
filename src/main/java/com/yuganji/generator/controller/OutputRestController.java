package com.yuganji.generator.controller;

import com.yuganji.generator.db.Output;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.util.NetUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = {"Provides API for Output's CRUD and controls"})
@RequestMapping(value = "/api")
@RestController
public class OutputRestController {

    private final OutputService outputService;

    @Autowired
    public OutputRestController(OutputService socketService) {
        this.outputService = socketService;
    }

    @ApiOperation(value = "List of output")
    @RequestMapping(value = "/outputs", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse list() {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", outputService.list());
    }

    @ApiOperation(value = "Getting information of Output")
    @ApiImplicitParam(name = "id", value = "Look up target ID", required = true, dataType = "int", example = "0")
    @RequestMapping(value = "/outputs/{id}", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse get(@PathVariable(value = "id") int id) {
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", outputService.get(id));
    }

    @ApiOperation(value = "Add Output")
    @ApiImplicitParam(name = "output", value = "Output details.", required = true, dataTypeClass = Output.class)
    @RequestMapping(value = "/outputs", method = RequestMethod.PUT)
    public @ResponseBody SingleObjectResponse add(
            @RequestBody Output output,
            HttpServletRequest request) {
        output.setIp(NetUtil.getClientIP(request));
        output.setId(null);
        return outputService.add(output);
    }

    @ApiOperation(value = "Modify Output")
    @ApiImplicitParam(name = "output", value = "Output details", required = true, dataTypeClass = Output.class)
    @RequestMapping(value = "/outputs", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse modify(
            @RequestBody Output output,
            HttpServletRequest request) {
        output.setIp(NetUtil.getClientIP(request));
        return outputService.modify(output);
    }

    @ApiOperation(value = "Remove Output")
    @ApiImplicitParam(name = "output", value = "Output details", required = true, dataTypeClass = Output.class)
    @RequestMapping(value = "/outputs", method = RequestMethod.DELETE)
    public @ResponseBody SingleObjectResponse remove(
            @RequestBody Output output,
            HttpServletRequest request) {
        output.setIp(NetUtil.getClientIP(request));
        return outputService.remove(output);
    }

    @ApiOperation(value = "Start Output")
    @ApiImplicitParam(name = "output", value = "Output details (Only Id)", required = true, dataTypeClass = Output.class)
    @RequestMapping(value = "/outputs/start", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse start(
            @RequestBody Output output,
            HttpServletRequest request) {
        output.setIp(NetUtil.getClientIP(request));
        return outputService.start(output);
    }

    @ApiOperation(value = "Stop Output")
    @ApiImplicitParam(name = "output", value = "Output details (Only Id)", required = true, dataTypeClass = Output.class)
    @RequestMapping(value = "/outputs/stop", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse stop(
            @RequestBody Output output,
            HttpServletRequest request) {
        output.setIp(NetUtil.getClientIP(request));
        return outputService.stop(output);
    }

    @ApiOperation(value = "Stop sparrow client")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "Output Id", required = true, dataType = "int", example = "0"),
            @ApiImplicitParam(name = "clientId", value = "Sparrow client id", required = true, dataType = "string")})
    @RequestMapping(value = "/outputs/stop-client/{id}/{clientId}", method = RequestMethod.PATCH)
    public @ResponseBody SingleObjectResponse stopClient(
            @PathVariable(value = "id") int id,
            @PathVariable(value = "clientId") String clientId,
            HttpServletRequest request) {
        return outputService.closeClient(id, clientId, NetUtil.getClientIP(request));
    }

    @ApiOperation(value = "Eps time series data from producer")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "Output Id", required = true, dataType = "int",
                    example = "0"),
            @ApiImplicitParam(name = "loggerId", value = "Logger Id",
                    dataTypeClass = Integer.class, example = "0")})
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
