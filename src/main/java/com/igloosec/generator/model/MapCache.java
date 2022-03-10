package com.igloosec.generator.model;

import com.igloosec.generator.engine.Ip2LocationService;

import lombok.Data;

@Data
public class MapCache {
    private Ip2LocationService ip2Locations;
}
