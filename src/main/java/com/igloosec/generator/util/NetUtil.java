package com.igloosec.generator.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.util.SubnetUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class NetUtil {
    private NetUtil() {
        throw new IllegalStateException("NetUtil is Utility class");
    }

    public static long ip2long(String str) {
        long result = 0L;
        try {
            String[] ipAddressInArray = str.toString().split("\\.");
            for (int i = 0; i < ipAddressInArray.length; i++) {
                int power = 3 - i;
                int ip = Integer.parseInt(ipAddressInArray[i]);
                result += ip * Math.pow(256, power);
            }
        } catch (Exception e) {
            log.warn(str + " is not ipv4 type.");
        }
        return result;
    }

    public static long[] getSubnet(String str) {
        long[] result = {0L, 0L};
        try {
            SubnetUtils subnet = new SubnetUtils(str);
            subnet.setInclusiveHostCount(true);
            return new long[] {
                    NetUtil.ip2long(subnet.getInfo().getLowAddress()),
                    NetUtil.ip2long(subnet.getInfo().getHighAddress())
            };
        } catch (Exception e) {
            log.warn(str + " is not ipv4 type.");
        }
        return result;
    }

    public static final String IP_REGEXP = "(\\d{1,}\\.){3}\\d{1,}";

    public static long[] getIpRanges(String str) {
        if (Pattern.matches(NetUtil.IP_REGEXP + "\\/\\d{1,}", str.trim())) {
            return NetUtil.getSubnet(str);
        } else if (Pattern.matches(NetUtil.IP_REGEXP + "(\\s{1,}|)\\~(\\s{1,}|)" + NetUtil.IP_REGEXP, str.trim())) {
            StringTokenizer token = new StringTokenizer(str, "~");
            List<Long> list = new ArrayList<>();
            while (token.hasMoreTokens()) {
                list.add(NetUtil.ip2long(token.nextToken().trim()));
            }
            return list.stream().mapToLong(x -> x).toArray();
        } else {
            return new long[] {
                NetUtil.ip2long(str),
                NetUtil.ip2long(str)
            };
        }
    }

    public static boolean compareIpRange(String ipRangeStr, String ip) {
        long[] ranges = NetUtil.getIpRanges(ipRangeStr);
        long realip = NetUtil.ip2long(ip);
        if (realip >= ranges[0] && realip <= ranges[1]) {
            return true;
        }
        return false;
    }
    
    public static String long2ip(long ip) {
        String result = "";
        while (ip > 0) {
            if (!"".equals(result)) {
                result = "." + result;
            }//w  w  w  . j a  v  a  2 s  .  c o m
            result = ip % 256 + result;
            ip = (long) Math.floor(ip / 256);
        }
        return result;
    }
    
    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
