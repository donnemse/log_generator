package com.igloosec.generator.field;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.igloosec.generator.util.Constants;

public class StrField extends FieldInfo implements IFieldGenerator {
    
    private List<String> keys;
    private List<Double> arr;
    private Random r;
    
    public StrField(Map<String, Double> values) {
        this.r = new Random();
        this.arr = new LinkedList<>();
        this.keys = new LinkedList<>();
        
        double sum = 0.0d;
        for (Entry<String, Double> entry: values.entrySet()) {
            arr.add(sum += entry.getValue() * Constants.D_THOUSAND);
            keys.add(entry.getKey());
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
        
        Object v = this.keys.get(idx);
        return new FieldValue(v, v);
    }
}
