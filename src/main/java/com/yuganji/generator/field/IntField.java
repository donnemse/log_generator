package com.yuganji.generator.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;
import com.yuganji.generator.util.Constants;

public class IntField extends FieldInfoVO implements IFieldGenerator {
    
    private List<String> keys;
    private List<Double> arr;
    private Random r;
    private Random intRandom;
    private int min = Integer.MAX_VALUE;
    private int max = 0;
    public IntField(Map<String, Double> values) {
        this.r = new Random();
        this.intRandom = new Random();
        this.arr = new LinkedList<>();
        this.keys = new LinkedList<>();
        
        double sum = 0.0d;
        for (Entry<String, Double> entry: values.entrySet()) {
            arr.add(sum += entry.getValue() * Constants.D_THOUSAND);
            keys.add(entry.getKey());
            
            StringTokenizer token = new StringTokenizer(entry.getKey(), "~|-");
            while (token.hasMoreTokens()) {
                int a = Integer.parseInt(token.nextToken());
                this.min = Math.min(min, a);
                this.max = Math.max(max, a);
            }
        }
        if (sum < Constants.D_THOUSAND) {
            arr.add(Constants.D_THOUSAND);
            keys.add(Constants.RANDOM_VALUE);
        }
    }
    
    @Override
    public FieldVO get() {
        double val =  r.nextInt(Constants.I_THOUSAND) * 1.d;
        int originIdx = Collections.binarySearch(arr, val);
        int idx = originIdx >= 0 ? originIdx : originIdx * -1 -1;
        
        int v = generateInt(this.keys.get(idx));
        return new FieldVO(v, v);
    }

    private int generateInt(String str) {
        if (str.equals(Constants.RANDOM_VALUE)) {
            return intRandom.ints(min, max + 1).findFirst().orElse(0);
        }
        int[] val = null;
        if (Pattern.matches("\\d{1,}(\\~|\\-)\\d{1,}", str.trim())) {
            StringTokenizer token = new StringTokenizer(str, "~|-");
            List<Integer> list = new ArrayList<>();
            while (token.hasMoreTokens()) {
                list.add(Integer.parseInt(token.nextToken().trim()));
            }
            val = list.stream().mapToInt(x -> x).toArray();
        } else {
            val = new int[] {
                    Integer.parseInt(str),
                    Integer.parseInt(str)
            };
        }
        // TODO Auto-generated method stub
        return intRandom.ints(val[0], val[1] + 1).findFirst().orElse(0);
    }
}
