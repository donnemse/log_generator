package com.igloosec.generator;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.prop.LoggerPropertyManager;
import com.igloosec.generator.service.output.OutputInfoVO;
import com.igloosec.generator.service.output.OutputService;

@Service
public class LogGeneratorStarter {
    @Autowired
    private LoggerPropertyManager logPropMng;
    
    @Autowired
    private OutputService socketService;
    
//    @Autowired
//    private Generator gen;
    
    @PostConstruct
    private void init() {
//            logPropMng.run();
//            gen.run();
        OutputInfoVO vo =new OutputInfoVO();
        vo.setPort(3305);
        vo.setMaxQueueSize(1000);
        socketService.open(vo);
    }
}
