package com.igloosec.generator.LogGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.generator.conf.LogProperty;
import com.igloosec.generator.field.FieldInfo;
import com.igloosec.generator.finnegan.Finnegan;
import com.igloosec.generator.util.NetUtil;


public class ApacheYamlTest {
    
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
        
        LogProperty a = mapper.readValue(new File("config/apache.yaml"), LogProperty.class);
        
        IntStream.range(1, 100).forEach(x -> {
//            a.generateLog()
           System.out.println(a.generateLog()); 
        });
        
        
    }
//    
}
