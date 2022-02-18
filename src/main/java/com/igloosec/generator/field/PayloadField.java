package com.igloosec.generator.field;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.igloosec.generator.finnegan.Finnegan;
import com.igloosec.generator.util.Constants;

public class PayloadField extends FieldInfo implements IFieldGenerator {
    
    private List<String> keys;
    private List<Double> arr;
    private Random r;
    private int minWord = Integer.MAX_VALUE;
    private int maxWord = 0;
    private Finnegan fin;
    private Random payloadRandom;
    
    public PayloadField(Map<String, Double> values) {
        this.r = new Random();
        this.payloadRandom = new Random();
        this.arr = new LinkedList<>();
        this.keys = new LinkedList<>();
        this.fin = Finnegan.ENGLISH;
        
        double sum = 0.0d;
        for (Entry<String, Double> entry: values.entrySet()) {
            arr.add(sum += entry.getValue() * Constants.D_THOUSAND);
            keys.add(entry.getKey());
            minWord = Math.min(minWord, entry.getKey().split(" ").length);
            maxWord = Math.max(maxWord, entry.getKey().split(" ").length);
        }
        if (sum < Constants.D_THOUSAND) {
            arr.add(Constants.D_THOUSAND);
            keys.add(Constants.RANDOM_VALUE);
        }
    }
    
    @Override
    public FieldValue get() {
        double val =  r.nextInt(Constants.I_THOUSAND) * 1.d;
        int originIdx = Collections.binarySearch(arr, val);
        int idx = originIdx >= 0 ? originIdx : originIdx * -1 -1;
        
        Object v = generatePayload(this.keys.get(idx));
        return new FieldValue(v, v);
    }

    private Object generatePayload(String str) {
        if (str.equals(Constants.RANDOM_VALUE)) {
            return fin.sentence(payloadRandom.nextLong(), minWord, maxWord, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.10);
        }
        return str;
    }
}
