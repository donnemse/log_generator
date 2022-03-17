package com.yuganji.generator.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yuganji.generator.engine.Ip2LocationService;
import com.yuganji.generator.model.*;
import com.yuganji.generator.mybatis.mapper.HistoryMapper;
import com.yuganji.generator.mybatis.mapper.LoggerMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class LoggerManager {
    private static final String TYPE = "logger";
    private final int SAMEPLE_CNT = 100; 
    private Map<Integer, LoggerVO> cache;
    private ObjectMapper om = new ObjectMapper(new YAMLFactory());
    
    @Autowired
    private LoggerMapper loggerMapper;
    
    @Autowired
    private HistoryMapper histMapper;
    
    @Autowired
    private Ip2LocationService ip2LocService;

    @PostConstruct
    private void init() {
        om.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        this.cache = new HashMap<>();
        List<LoggerVO> listInfo = this.loggerMapper.listLogger();
        this.cache = listInfo.stream()
            .collect(Collectors.toMap(LoggerVO::getId, x -> {
                try {
                    LoggerPropVO lp = om.readValue(x.getYamlStr(), LoggerPropVO.class);
                    x.setLogger(lp);
                    
                    MapCache mapCache = new MapCache();
                    mapCache.setIp2Locations(ip2LocService);
                    x.setMapCache(mapCache);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage(), e);
                }
                return x;
            }));
    }

    public LoggerVO getLogger(int id) {
        return this.cache.get(id);
    }
    
    public Map<Integer, LoggerVO> listLogger() {
        return this.cache;
    }

    public SingleObjectResponse createLogger(LoggerRequestVO vo) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        try {
            LoggerPropVO lp = om.readValue(vo.getYaml(), LoggerPropVO.class);
            LoggerVO info = new LoggerVO();
            info.setLogger(lp);
            info.setName(vo.getName());
            info.setYamlStr(vo.getYaml());
            info.setIp(vo.getIp());
            info.setCreated(new Date().getTime());
            info.setLastModified(new Date().getTime());
            MapCache mapCache = new MapCache();
            mapCache.setIp2Locations(ip2LocService);
            info.setMapCache(mapCache);
            
            loggerMapper.insertLogger(info);
            this.cache.put(info.getId(), info);
            res.setMsg("Successfully saved " + vo.getName());
            res.setData(info);
            vo.setId(info.getId());
            this.addHistory(vo, "Successfully saved " + vo.getName(), vo.getYaml(), null);
//            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            res.setMsg(e.getMessage());
            this.addHistory(vo, "Failed save logger " + vo.getName(), null, e.getMessage());
        }
        return res;
    }

    public SingleObjectResponse modifyLogger(LoggerRequestVO vo) {
        String msg = "logger was modified. " + vo.getName();
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value(), msg);
        if (!this.cache.containsKey(vo.getId())) {
            res.setMsg("can not found logger. " + vo.getName());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return res;
        } else if (this.cache.get(vo.getId()).getStatus() == 1) {
            res.setMsg(vo.getName() + " is running now. stop it first.");
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return res;
        }
        try {
            // TODO Stop logging
            LoggerPropVO lp = om.readValue(vo.getYaml(), LoggerPropVO.class);
            // TODO validateCheck
            this.cache.remove(vo.getId());
            LoggerVO info = new LoggerVO();
            info.setLogger(lp);
            info.setId(vo.getId());
            info.setIp(vo.getIp());
            info.setName(vo.getName());
            info.setLastModified(new Date().getTime());
            info.setYamlStr(vo.getYaml());
            MapCache mapCache = new MapCache();
            mapCache.setIp2Locations(ip2LocService);
            info.setMapCache(mapCache);
            loggerMapper.updateLogger(info);
            this.addHistory(vo, msg, vo.getYaml(), null);
            this.cache.put(vo.getId(), info);
            res.setMsg(msg);
            res.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.addHistory(vo, "could not modified logger. " + vo.getName(), null, e.getMessage());
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public SingleObjectResponse removeLogger(LoggerRequestVO vo) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        try {
            LoggerVO info = this.cache.get(vo.getId());
            String msg = "logger was removed. " + info.getName();
            res.setMsg(msg);
            
            this.loggerMapper.removeLogger(vo.getId());
            this.addHistory(vo, msg, info.getYamlStr(), null);
            this.cache.remove(vo.getId());
        } catch (Exception e) {
            this.addHistory(vo, "could not remove logger. " + vo.getName(), null, e.getMessage());
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    
    public List<Map<String, Object>> sample(LoggerRequestVO vo) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LoggerPropVO lp = om.readValue(vo.getYaml(), LoggerPropVO.class);
            MapCache mapCache = new MapCache();
            mapCache.setIp2Locations(ip2LocService);
            lp.setMapCache(mapCache);
            for (int i = 0; i < SAMEPLE_CNT; i++) {
                list.add(lp.generateLog());
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Map<String, Object> map = new HashMap<>();
            map.put("error", sw.toString());
            list.add(map);
        }
        return list;
    }

    public boolean addHistory(LoggerRequestVO vo, String msg, String detail, String error) {
        histMapper.insertHistory(vo.getId(), vo.getIp(), TYPE, new Date().getTime(), msg, detail, null);
        return true;
    }
}
