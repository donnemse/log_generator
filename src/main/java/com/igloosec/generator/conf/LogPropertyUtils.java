package com.igloosec.generator.conf;

import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class LogPropertyUtils extends PropertyUtils {

    @Override
    public Property getProperty(Class<? extends Object> type, String name) {
        System.out.println(type + " " + name);
        if (name.equals("data")) {
            
            return null;
        }
        return super.getProperty(type, name);
    }
    
}
