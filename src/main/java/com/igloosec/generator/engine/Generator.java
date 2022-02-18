package com.igloosec.generator.engine;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.generator.prop.LogProperty;
import com.igloosec.generator.queue.LogQueueService;

@Service
@EnableAsync
public class Generator {
    @Autowired
    private LogQueueService queueService;
    
    @PostConstruct
    public void init() throws StreamReadException, DatabindException, IOException {
        
        startGen();
//        IntStream.range(1, 100).forEach(x -> {
//           System.out.println(); 
//        });
    }
    @Async
    private void startGen() throws StreamReadException, DatabindException, IOException {
        Thread t = new Thread(() -> {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            
            try {
                LogProperty a = mapper.readValue(new File("config/apache.yaml"), LogProperty.class);
                while(true) {
                    Map<String, Object> b = a.generateLog();
                    queueService.pushLog(b);
                    System.out.println(b);
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        t.start();
        
    }
}
