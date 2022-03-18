package com.yuganji.generator.model;

import org.apache.commons.lang3.RandomUtils;

import lombok.Data;

@Data
public class IntBound {
    
    private int min;
    private int max;
    
    public IntBound(int min, int max) {
        this.min = min;
        this.max = max;
        if (this.min > this.max) {
            throw new IllegalArgumentException("min value is bigger than max value");
        }
    }
            
    public IntBound(String str) {
        String[] arr = str.split("~|-");
        if (arr.length == 2) {
            this.min = Integer.parseInt(arr[0]);
            this.max = Integer.parseInt(arr[1]) + 1;
        } else if (arr.length == 1) {
            this.min = Integer.parseInt(str);
            this.max = Integer.parseInt(str) + 1;
        } else {
            throw new IllegalArgumentException(str + " is not number range format");
        }
        if (this.min > this.max) {
            throw new IllegalArgumentException("min value is bigger than max value");
        }
    }
    
    public int randomInt() {
        return RandomUtils.nextInt(this.min, this.max);
    }
}
