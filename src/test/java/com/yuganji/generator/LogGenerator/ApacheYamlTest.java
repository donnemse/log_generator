package com.yuganji.generator.LogGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;

import com.yuganji.generator.util.NetUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yuganji.generator.engine.Ip2LocationService;
import com.yuganji.generator.field.IDField;
import com.yuganji.generator.finnegan.Finnegan;
import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.LoggerPropVO;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class ApacheYamlTest {
    
    @Autowired
    Ip2LocationService ip2loc;
    
    @Test
    public void test3() {
        long time = System.currentTimeMillis();
        log.debug(ip2loc.getLocation("175.119.119.195").getCode());
        System.out.println(System.currentTimeMillis() - time);
    }
    
    @Test
    public void test2() {
        FieldInfoVO vo = new IDField(null);
        IntStream.range(0,  10).forEach(x -> {
            try {
                System.out.println(vo.get().getValue());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        
    }
//    @Test
    public void test1() {
        
        long[] range = NetUtil.getIpRanges("192.168.1.1");
        Random ipRandom = new Random();
        
        System.out.println("**********");
        System.out.println(range[0]);
        System.out.println(range[1]);
        Finnegan fin = Finnegan.ENGLISH;
        IntStream.rangeClosed(1, 100).forEach(x->{
            
            System.out.println(fin.sentence(1, 3, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.10));
//            System.out.println(
//                    NetUtil.long2ip(ipRandom.longs(range[0], range[1] + 1).findFirst().getAsLong()));
        });

    }
    @Test
    public void test() throws StreamReadException, DatabindException, IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        
        LoggerPropVO a = mapper.readValue(new File("config/apache.yaml"), LoggerPropVO.class);
        
        IntStream.range(1, 100).forEach(x -> {
//            a.generateLog()
           try {
            System.out.println(a.generateLog());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        });
    }
//    
}
