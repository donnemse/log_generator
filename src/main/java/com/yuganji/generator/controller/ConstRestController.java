package com.yuganji.generator.controller;

import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/const")
public class ConstRestController {

    @Autowired
    public ConstRestController() {

    }
    
    @RequestMapping(value = "/output/template", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse template() {
        Map<String, OutputTemplate> template = new HashMap<>();
        template.put("name",
                new OutputTemplate("Name", "New Output", Constants.InputForm.TEXT.getValue(), null));
        template.put("max_queue_size",
                new OutputTemplate("Max Queue", "100000", Constants.InputForm.TEXT.getValue(), null));
        template.put("type",
                new OutputTemplate("Type", "select", Constants.InputForm.SELECT.getValue(),new HashMap<>()));

        for (Constants.OutputType outputType: Constants.OutputType.values()){
            template.get("type").getInfo().putAll(outputType.getValue());
        }
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", template);


    }
}
