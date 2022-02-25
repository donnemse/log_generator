package com.igloosec.generator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.output.OutputService;
import com.igloosec.generator.restful.model.OutputInfoVO;

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
        OutputInfoVO vo =new OutputInfoVO();
        vo.setPort(3305);
        vo.setMaxQueueSize(1000);
        socketService.open(vo);
    }
}
