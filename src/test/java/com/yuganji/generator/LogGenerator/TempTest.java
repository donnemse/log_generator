package com.yuganji.generator.LogGenerator;

import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.stream.IntStream;

public class TempTest {

    @Test
    public void test5() {
        Random random = new Random();
        IntStream.range(0, 10).forEach(x -> {
            System.out.println(random.ints(500, 500));
        });

    }
    @Test
    public void test4(){
        String[] arr = new String[] {
                "10~100",
                "1-10",
                "500",
//                "100-1",
                "500-500-300",
        };
//        int[] eps = new int[2];
        Range<Integer> eps;
        for (String a: arr) {
            String[] sp = a.split("-|~");
            if (sp.length == 2){
                eps = Range.closed(Integer.parseInt(sp[0]), Integer.parseInt(sp[1]));
            } else if (sp.length == 1){
                eps = Range.closed(Integer.parseInt(a), Integer.parseInt(a));
            } else {
                throw new IllegalArgumentException("Invalid range: " + a);
            }
            System.out.println(eps.lowerEndpoint() + " ~ " + eps.upperEndpoint());
        }
    }

    @Test
    public void test() {
        long time = System.currentTimeMillis();
//        time = time - (time % (1000 * 60 * 20));
        time = time / (1000 * 60 * 10) * (1000 * 60 * 10);
        System.out.println(new Date(time));

        System.out.println(new SimpleDateFormat("yyyyMMddH0").format(new Date()));
    }
}
