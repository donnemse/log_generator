package com.yuganji.generator.field;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.concurrent.ThreadLocalRandom;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;
import com.yuganji.generator.util.Constants;

public class StrField extends FieldInfoVO implements IFieldGenerator {
    
    private List<String> keys;
    private List<Double> arr;
    private FieldVO[] prebuilt;

    public StrField(Map<String, Double> values) {
        this.arr = new ArrayList<>();
        this.keys = new ArrayList<>();

        double sum = 0.0d;
        for (Entry<String, Double> entry: values.entrySet()) {
            arr.add(sum += entry.getValue() * Constants.D_THOUSAND);
            keys.add(entry.getKey());
        }
        if (sum < Constants.D_THOUSAND) {
            arr.add(Constants.D_THOUSAND);
            keys.add(Constants.RANDOM_VALUE);
        }
        this.prebuilt = new FieldVO[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            prebuilt[i] = new FieldVO(keys.get(i), keys.get(i));
        }
    }

    @Override
    public FieldVO get() {
        double val = ThreadLocalRandom.current().nextInt(0, Constants.I_THOUSAND) * 1.d;
        int originIdx = Collections.binarySearch(arr, val);
        int idx = originIdx >= 0 ? originIdx : originIdx * -1 -1;

        return this.prebuilt[idx];
    }
}
