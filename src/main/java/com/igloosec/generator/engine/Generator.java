package com.igloosec.generator.engine;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.generator.prop.LoggerProperty;
import com.igloosec.generator.queue.LogQueueService;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Service
@Log4j2
public class Generator {
    @Autowired
    private LogQueueService queueService;
    
    @Async
//    @PostConstruct
    public void run() throws StreamReadException, DatabindException, IOException {
//        
//        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        
//        try {
//            LogProperty a = mapper.readValue(new File("config/apache.yaml"), LogProperty.class);
//            while(true) {
//                Map<String, Object> b = a.generateLog();
//                queueService.pushLog(b);
//                log.debug(b);
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        } catch (IOException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
    }
}
