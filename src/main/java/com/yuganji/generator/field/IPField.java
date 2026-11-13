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
import com.yuganji.generator.util.NetUtil;

public class IPField extends FieldInfoVO implements IFieldGenerator {

    private static final long[] FULL_RANGE = {0L, 4294967295L};

    private List<String> keys;
    private List<Double> arr;
    private List<long[]> cachedRanges;
    
    public IPField(Map<String, Double> values) {
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
        this.cachedRanges = new ArrayList<>();
        for (String key : this.keys) {
            if (key.equals(Constants.RANDOM_VALUE)) {
                this.cachedRanges.add(FULL_RANGE);
            } else {
                this.cachedRanges.add(NetUtil.getIpRanges(key));
            }
        }
    }
    
    @Override
    public FieldVO get() {
        double val = ThreadLocalRandom.current().nextInt(0, Constants.I_THOUSAND) * 1.d;
        int originIdx = Collections.binarySearch(arr, val);
        int idx = originIdx >= 0 ? originIdx : originIdx * -1 -1;
        
        String v = this.generateIp(idx);
        return new FieldVO(v, v);
    }

    private String generateIp(int idx) {
        long[] range = this.cachedRanges.get(idx);
        return NetUtil.long2ip(ThreadLocalRandom.current().nextLong(range[0], range[1] + 1));
    }
}
