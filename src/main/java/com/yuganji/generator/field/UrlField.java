package com.yuganji.generator.field;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;

import com.yuganji.generator.finnegan.Finnegan;
import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;
import com.yuganji.generator.util.Constants;

public class UrlField extends FieldInfoVO implements IFieldGenerator {
    
    private List<String> keys;
    private List<Double> arr;
    private int minWord = Integer.MAX_VALUE;
    private int maxWord = 0;
    private Finnegan fin;
    private String[] extentions = new String[] {".jsp", ".xml", ".js"};
    
    public UrlField(Map<String, Double> values) {
        this.arr = new LinkedList<>();
        this.keys = new LinkedList<>();
        this.fin = Finnegan.ENGLISH;
        
        double sum = 0.0d;
        for (Entry<String, Double> entry: values.entrySet()) {
            arr.add(sum += entry.getValue() * Constants.D_THOUSAND);
            keys.add(entry.getKey());
            minWord = Math.min(minWord, entry.getKey().split("/").length);
            maxWord = Math.max(maxWord, entry.getKey().split("/").length);
        }
        if (sum < Constants.D_THOUSAND) {
            arr.add(Constants.D_THOUSAND);
            keys.add(Constants.RANDOM_VALUE);
        }
    }
    
    @Override
    public FieldVO get() {
        double val = RandomUtils.nextInt(0, Constants.I_THOUSAND) * 1.d;
        int originIdx = Collections.binarySearch(arr, val);
        int idx = originIdx >= 0 ? originIdx : originIdx * -1 -1;
        
        String v = generatePayload(this.keys.get(idx));
        return new FieldVO(v, v);
    }

    private String generatePayload(String str) {
        if (str.equals(Constants.RANDOM_VALUE)) {
            String url = "/" + fin.sentence(
                    RandomUtils.nextLong(), minWord, maxWord, 
                    new String[]{"/"},
                    this.extentions, 1).replaceAll(" ", "");
            int param = RandomUtils.nextInt(0, 5);
            if (param > 0) {
                String[] p = new String[param];
                url += "?" + String.join("&",
                    Stream.of(p).map(x -> 
                    fin.word(RandomUtils.nextLong(), false, 1) + "=" + fin.word(RandomUtils.nextLong(), false, 1))
                    .toArray(String[]::new));
            }
            return url;
        }
        return str;
    }
}
