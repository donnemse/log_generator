package com.igloosec.generator.engine;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.igloosec.generator.model.IpLocationVO;
import com.igloosec.generator.util.NetUtil;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class Ip2LocationService {
    
    private List<IpLocationVO> list;
    
    @Value("${file.path.ip2location:./config/IPCountry.csv}")
    private String filePath;
    
    @PostConstruct
    public void init() {
        this.list = new LinkedList<>();
        CsvParser parser = new CsvParser(new CsvParserSettings());
        parser.beginParsing(new File(filePath));
        
        Record r = null;
        while ((r = parser.parseNextRecord()) != null) {
            this.list.add(new IpLocationVO(r.getString(2), r.getString(3), r.getLong(0), r.getLong(1)));
        }
        log.debug("Loaded Ip2Locations.");
        Collections.sort(this.list, new Comparator<IpLocationVO>() {
            @Override
            public int compare(IpLocationVO o1, IpLocationVO o2) {
                return ((Long) o1.getSip()).compareTo((Long) o2.getSip());
            }
        });
    }
    
    public IpLocationVO getLocation(String ip) {
        long longIp = NetUtil.ip2long(ip);
        int idx = Collections.binarySearch(this.list, longIp);
        if(idx < 0) {
            return new IpLocationVO("-", "-", longIp, longIp);
        }
        return list.get(idx);
    }
}
