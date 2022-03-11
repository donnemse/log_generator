package com.yuganji.generator.util;

import lombok.Getter;

/**
 * @author yuganji
 *
 */
public final class Constants {
    private Constants() {
        throw new IllegalStateException("Constants is utility class");
    }
    public static final long MILLIS_1SEC = 1000;
    public static final long MILLIS_1MIN = MILLIS_1SEC * 60;
    public static final long MILLIS_1HOUR = MILLIS_1MIN * 60;
    public static final long MILLIS_1DAY = MILLIS_1HOUR * 24;
    
    public static final double D_THOUSAND = 1_000.D;
    public static final int I_THOUSAND = 1_000;
    public static final String RANDOM_VALUE = "_RANDOM";
    

    public static final String UTF_BOM = "\uFEFF";

    public enum DataType {
        IP2LOC("ip2loc"),
        SPARROW_ID("sparrow_id"),
        IP("ip"),
        TIME("time"),
        STR("str"),
        INT("int"),
        DOUBLE("double"),
        PAYLOAD("payload"),
        URL("url"),
        Long("long");
        
        @Getter
        private final String value;
        DataType(String val) {
            this.value = val;
        }
    }
}
