package com.yuganji.generator.util;

import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Log4j2
public final class CommonUtil {
    private CommonUtil() {
        throw new IllegalStateException("CommonUtil is Utility class");
    }
    public static int calcObjectSize(Object obj) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        int size = 0;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            size = baos.size();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return size;
    }
}
