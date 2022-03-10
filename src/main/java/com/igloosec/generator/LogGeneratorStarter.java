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
    
    @PostConstruct
    private void init() {
        OutputInfoVO vo =new OutputInfoVO(3305, 100000);
        socketService.open(vo);
    }
}
