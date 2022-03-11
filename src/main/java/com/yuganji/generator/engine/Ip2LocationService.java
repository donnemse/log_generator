package com.yuganji.generator.engine;

import java.io.File;

import javax.annotation.PostConstruct;

import com.yuganji.generator.util.NetUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.yuganji.generator.model.IpLocationVO;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class Ip2LocationService {
    RangeMap<Long, IpLocationVO> rangeMap;
    
    @Value("${file.path.ip2location:./config/IPCountry.csv}")
    private String filePath;
    
    @PostConstruct
    public void init() {
        CsvParser parser = new CsvParser(new CsvParserSettings());
        this.rangeMap = TreeRangeMap.create();
        parser.beginParsing(new File(filePath));
        
        Record r = null;
        while ((r = parser.parseNextRecord()) != null) {
            rangeMap.put(Range.closed(r.getLong(0), r.getLong(1)),
                    new IpLocationVO(r.getString(2), r.getString(3), r.getLong(0), r.getLong(1))
                    );
        }
        log.debug("Loaded Ip2Locations.");
    }
    
    public IpLocationVO getLocation(String ip) {
        long longIp = NetUtil.ip2long(ip);
        IpLocationVO res = this.rangeMap.get(longIp);
        
        if (res == null) {
            return new IpLocationVO("-", "-", longIp, longIp);
        }
        return res;
    }
}
