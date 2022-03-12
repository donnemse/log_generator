package com.yuganji.generator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yuganji.generator.model.OutputVO;
import com.yuganji.generator.model.SparrowOutput;
import com.yuganji.generator.output.OutputService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LogGeneratorStarter {

//    private OutputService outputService;

//    @Autowired
    public LogGeneratorStarter() {
        
//        this.outputService = ApplicationContextProvider.getApplicationContext().getBean(OutputService.class);
    }
    @PostConstruct
    private void init() {
//        OutputVO vo =new OutputVO(3305, 100000);
//        outputService.startOutput(vo);
        
//        OutputVO vo = new OutputVO();
//        vo.setId(1);
//        vo.setType("sparrow");
//        vo.setName("Test");
//        vo.setHandler(new SparrowOutput(3305));
//        outputService.getCache().put(1, vo);
//        outputService.startOutput(vo);
//        log.debug(outputService.list());
    }
}
