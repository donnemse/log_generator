package com.yuganji.generator.field;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomUtils;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;
import com.yuganji.generator.model.IntBound;
import com.yuganji.generator.util.Constants;

public class IntField extends FieldInfoVO implements IFieldGenerator {
    
    private List<IntBound> keys;
    private List<Double> arr;
    
    public IntField(Map<String, Double> values) {
        this.arr = new LinkedList<>();
        this.keys = new LinkedList<>();

        int min = Integer.MAX_VALUE;
        int max = 0;
        
        double sum = 0.0d;
        for (Entry<String, Double> entry: values.entrySet()) {
            arr.add(sum += entry.getValue() * Constants.D_THOUSAND);
            IntBound bound = new IntBound(entry.getKey());
            keys.add(bound);
            
            min = Math.min(min, bound.getMin());
            max = Math.max(max, bound.getMax());
        }
        if (sum < Constants.D_THOUSAND) {
            arr.add(Constants.D_THOUSAND);
            keys.add(new IntBound(min, max));
        }
    }
    
    @Override
    public FieldVO get() {
        double val = RandomUtils.nextInt(0, Constants.I_THOUSAND) * 1.d;
        int originIdx = Collections.binarySearch(arr, val);
        int idx = originIdx >= 0 ? originIdx : originIdx * -1 -1;
        
        int v = generateInt(this.keys.get(idx));
        return new FieldVO(v, v);
    }

    private int generateInt(IntBound bound) {
        return bound.randomInt();
    }
}
