package com.yuganji.generator.controller;

import com.yuganji.generator.db.KafkaSettings;
import com.yuganji.generator.db.KafkaSettingsRepository;
import com.yuganji.generator.model.SingleObjectResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"Global Kafka Settings"})
@RestController
@RequestMapping(value = "/api")
public class KafkaSettingsRestController {

    @Autowired
    private KafkaSettingsRepository kafkaSettingsRepository;

    @ApiOperation(value = "Get global Kafka settings")
    @RequestMapping(value = "/kafka/settings", method = RequestMethod.GET)
    public @ResponseBody SingleObjectResponse getSettings() {
        KafkaSettings settings = kafkaSettingsRepository.findById(1).orElse(null);
        return new SingleObjectResponse(HttpStatus.OK.value(), "OK", settings);
    }

    @ApiOperation(value = "Save global Kafka settings")
    @RequestMapping(value = "/kafka/settings", method = RequestMethod.POST)
    public @ResponseBody SingleObjectResponse saveSettings(@RequestBody KafkaSettings settings) {
        settings.setId(1);  // Always single-row
        kafkaSettingsRepository.save(settings);
        return new SingleObjectResponse(HttpStatus.OK.value(), "Kafka settings saved", settings);
    }
}
