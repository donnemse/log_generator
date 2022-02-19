package com.igloosec.generator;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.engine.Generator;
import com.igloosec.generator.prop.LoggerPropertyManager;

@Service
public class LogGeneratorStarter {
    @Autowired
    private LoggerPropertyManager logPropMng;
    
    @Autowired
    private Generator gen;
    @PostConstruct
    private void init() {
        try {
            logPropMng.run();
            gen.run();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
