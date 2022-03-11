package com.yuganji.generator.LogGenerator;

import org.junit.jupiter.api.Test;

import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.model.SparrowOutput;

public class TempTest {

    @Test
    public void test() {
        SparrowOutput vo = new SparrowOutput(1000);
        vo.setPort(1000);
        
        AbstractOutputHandler vo2 = vo;
        
        System.out.println(((SparrowOutput) vo2).getPort());
        
    }
}
