package com.yuganji.generator.field;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomUtils;

import com.yuganji.generator.model.FieldInfoVO;
import com.yuganji.generator.model.FieldVO;
import com.yuganji.generator.util.Constants;
import com.yuganji.generator.util.NetUtil;

public class IPField extends FieldInfoVO implements IFieldGenerator {
    
    private List<String> keys;
    private List<Double> arr;
    
    public IPField(Map<String, Double> values) {
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
    public FieldVO get() {
        double val = RandomUtils.nextInt(0, Constants.I_THOUSAND) * 1.d;
        int originIdx = Collections.binarySearch(arr, val);
        int idx = originIdx >= 0 ? originIdx : originIdx * -1 -1;
        
        String v = this.generateIp(this.keys.get(idx));
        return new FieldVO(v, v);
    }

    private String generateIp(String str) {
        long[] range = null;
        if (str.equals(Constants.RANDOM_VALUE)) {
            range = new long[] {
                    NetUtil.ip2long("0.0.0.0"),
                    NetUtil.ip2long("255.255.255.255")
            };
        } else {
            range = NetUtil.getIpRanges(str);
        }
        return NetUtil.long2ip(RandomUtils.nextLong(range[0], range[1] + 1));
    }
}
