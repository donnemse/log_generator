package com.igloosec.generator;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.prop.LoggerPropertyManager;
import com.igloosec.generator.service.socket.SocketInfoVO;
import com.igloosec.generator.service.socket.SocketService;

@Service
public class LogGeneratorStarter {
    @Autowired
    private LoggerPropertyManager logPropMng;
    
    @Autowired
    private SocketService socketService;
    
//    @Autowired
//    private Generator gen;
    
    @PostConstruct
    private void init() {
        try {
            logPropMng.run();
//            gen.run();
            SocketInfoVO vo =new SocketInfoVO();
            vo.setPort(3000);
            vo.setMaxQueueSize(1000);
            socketService.open(vo);
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
