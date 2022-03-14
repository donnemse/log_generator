package com.yuganji.generator.LogGenerator;

import org.junit.jupiter.api.Test;

import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.output.model.SparrowOutput;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TempTest {

    @Test
    public void test() {
        long time = System.currentTimeMillis();
//        time = time - (time % (1000 * 60 * 20));
        time = time / (1000 * 60 * 10) * (1000 * 60 * 10);
        System.out.println(new Date(time));


        System.out.println(new SimpleDateFormat("yyyyMMddH0").format(new Date()));
        SparrowOutput vo = new SparrowOutput(1000);
        vo.setPort(1000);
        
        AbstractOutputHandler vo2 = vo;

        System.out.println(((SparrowOutput) vo2).getPort());
        
    }
}
