package com.igloosec.generator.prop;

import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoggerPropertyUtils extends PropertyUtils {

    @Override
    public Property getProperty(Class<? extends Object> type, String name) {
        log.debug(type + " " + name);
        if (name.equals("data")) {
            
            return null;
        }
        return super.getProperty(type, name);
    }
    
}
