package com.yuganji.generator.LogGenerator;

import java.io.File;
import java.util.stream.IntStream;

import com.yuganji.generator.util.NetUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.yuganji.generator.model.IpLocationVO;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BinarySearchTest {
    
    @Value("${file.path.ip2location:./config/IPCountry.csv}")
    private String filePath;
    
    RangeMap<Long, IpLocationVO> rangeMap;
    
    public void init() {
        CsvParser parser = new CsvParser(new CsvParserSettings());
        parser.beginParsing(new File("./config/IPCountry.csv"));
        
        rangeMap = TreeRangeMap.create();
        
        Record r = null;
        while ((r = parser.parseNextRecord()) != null) {
            rangeMap.put(Range.closed(r.getLong(0), r.getLong(1)),
                    new IpLocationVO(r.getString(2), r.getString(3), r.getLong(0), r.getLong(1))
                    );
        }
        
        long time = System.currentTimeMillis();

        log.debug(System.currentTimeMillis() - time);
        log.debug("Loaded Ip2Locations.");
    }
    
    public IpLocationVO getLocation(String ip) {
        long longIp = NetUtil.ip2long(ip);
        return this.rangeMap.get(longIp);
    }
    
    @Test
    public void test() {
        init();
        long time = System.currentTimeMillis();
        IntStream.range(0, 30000).forEach(x -> {
            this.getLocation("61.34.170.150").getCode();
        });
        
        log.debug(this.getLocation(null));
        log.debug("time = " + (System.currentTimeMillis() - time));
   }

    @Test
    public void q() {
        
        log.debug(Math.sqrt(1000));
   }
}
