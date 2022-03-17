package com.yuganji.generator.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yuganji.generator.db.Logger;
import com.yuganji.generator.db.LoggerRepository;
import com.yuganji.generator.engine.Ip2LocationService;
import com.yuganji.generator.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class LoggerService {
    private static final String TYPE = "logger";
    private final int SAMEPLE_CNT = 100; 
    private Map<Integer, LoggerDto> cache;
    private ObjectMapper om = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @Autowired
    private LoggerRepository loggerRepository;

    @Autowired
    private Ip2LocationService ip2LocService;

    @PostConstruct
    private void init() {
        this.cache = loggerRepository.findAll().stream().collect(
                Collectors.toMap(Logger::getId, x -> x.toDto()));
    }

    public LoggerDto get(int id) {
        return this.cache.get(id);
    }
    
    public Map<Integer, LoggerDto> list() {
        return this.cache;
    }

    public SingleObjectResponse add(Logger logger) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        try {
            logger = loggerRepository.save(logger);
            LoggerDto loggerDto = logger.toDto();
            this.cache.put(logger.getId(), loggerDto);
            res.setMsg("Successfully saved " + logger.getName());
            res.setData(loggerDto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            res.setMsg(e.getMessage());
//            this.addHistory(entity, "Failed save logger " + entity.getName(), null, e.getMessage());
        }
        return res;
    }

    public SingleObjectResponse modify(Logger logger) {
        String msg = "logger was modified. " + logger.getName();
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value(), msg);
        if (!this.cache.containsKey(logger.getId())) {
            res.setMsg("can not found logger. " + logger.getName());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return res;
        } else if (this.cache.get(logger.getId()).getStatus() == 1) {
            res.setMsg(logger.getName() + " is running now. stop it first.");
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return res;
        }
        try {
            loggerRepository.save(logger);
//            this.addHistory(logger, msg, logger.getYaml(), null);
            this.cache.put(logger.getId(), logger.toDto());
            res.setMsg(msg);
            res.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
//            this.addHistory(logger, "could not modified logger. " + logger.getName(), null, e.getMessage());
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public SingleObjectResponse remove(Logger logger) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        try {
            LoggerDto info = this.cache.get(logger.getId());
            String msg = "logger was removed. " + info.getName();
            res.setMsg(msg);
            loggerRepository.deleteById(logger.getId());
//            this.addHistory(logger, msg, info.getYamlStr(), null);
            this.cache.remove(logger.getId());
        } catch (Exception e) {
//            this.addHistory(logger, "could not remove logger. " + logger.getName(), null, e.getMessage());
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    
    public List<Map<String, Object>> sample(LoggerRequestVO vo) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LoggerDetailDto lp = om.readValue(vo.getYaml(), LoggerDetailDto.class);
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
//        histMapper.insertHistory(vo.getId(), vo.getIp(), TYPE, new Date().getTime(), msg, detail, null);
        return true;
    }
}