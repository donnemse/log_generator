package com.igloosec.generator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.model.OutputInfoVO;
import com.igloosec.generator.output.OutputService;

@Service
public class LogGeneratorStarter {
    
    @Autowired
    private OutputService socketService;
    
//    @Autowired
//    private Generator gen;
    
    @PostConstruct
    private void init() {
//            logPropMng.run();
//            gen.run();
        OutputInfoVO vo =new OutputInfoVO(3305, 1000);
        socketService.open(vo);
    }
}
