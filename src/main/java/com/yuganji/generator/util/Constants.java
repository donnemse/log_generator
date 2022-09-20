package com.yuganji.generator.util;

import com.yuganji.generator.controller.OutputTemplate;
import lombok.Getter;

import java.util.*;

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
    
    public enum Output {
        SPARROW("sparrow"),
        KAFKA("kafka"),
        FILE("file");
        
        @Getter
        private final String value;
        Output(String val) {
            this.value = val;
        }
    }

    public enum InputForm {
        TEXT("text"),
        SELECT("select"),
        CHECK("check");

        @Getter
        private final String value;
        InputForm(String val) {
            this.value = val;
        }
    }

    public enum FileOutputType {
        CSV("csv"),
        JSON("json"),
        RAW("raw");

        @Getter
        private final String value;
        FileOutputType(String val) {
            this.value = val;
        }
    }

    public enum OutputType {
        SPARROW(new HashMap<String, List<OutputTemplate>>(){{
            put("sparrow", Collections.singletonList(
                    new OutputTemplate("port", "3303", InputForm.TEXT.getValue(), null)));
        }}),
        KAFKA(new HashMap<String, List<OutputTemplate>>(){{
            put("kafka", Arrays.asList(
                    new OutputTemplate("bootstrap_servers", "prep1:9092,prep2:9092,prep3:9092", InputForm.TEXT.getValue(), null),
                    new OutputTemplate("topic_name", "test-topic", InputForm.TEXT.getValue(), null),
                    new OutputTemplate("output_type", "csv", InputForm.TEXT.getValue(), null),
                    new OutputTemplate("batch_size", "10", InputForm.TEXT.getValue(), null),
                    new OutputTemplate("filter", "logger1", InputForm.TEXT.getValue(), null)));
        }}),
        FILE(new HashMap<String, List<OutputTemplate>>(){{
            put("file", Arrays.asList(
                new OutputTemplate("path", "./generate_log", InputForm.TEXT.getValue(), null),
                new OutputTemplate("file_prefix", "logtype", InputForm.TEXT.getValue(), null),
                new OutputTemplate("output_type", "output_type", InputForm.SELECT.getValue(),
                        new HashMap<String, List<OutputTemplate>>(){{
                            put(FileOutputType.CSV.getValue(), new ArrayList<>());
                            put(FileOutputType.JSON.getValue(), new ArrayList<>());
                            put(FileOutputType.RAW.getValue(), new ArrayList<>());
                }}),
                new OutputTemplate("file_rotation_min", "10 - Rotation file by minute", InputForm.TEXT.getValue(), null),
                new OutputTemplate("max_size", "104857600 - Max size per file", InputForm.TEXT.getValue(), null),
                new OutputTemplate("batch_size", "10", InputForm.TEXT.getValue(), null)));
        }});

        @Getter
        private final Map<String, List<OutputTemplate>> value;
        OutputType(Map<String, List<OutputTemplate>> val) {
            this.value = val;
        }
    }
}
