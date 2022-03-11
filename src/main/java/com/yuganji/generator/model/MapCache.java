package com.yuganji.generator.model;

import com.yuganji.generator.engine.Ip2LocationService;

import lombok.Data;

@Data
public class MapCache {
    private Ip2LocationService ip2Locations;
}
