package com.yuganji.generator.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.yuganji.generator.db.Logger;
import com.yuganji.generator.db.LoggerRepository;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.SingleObjectResponse;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LoggerService {
    private final int SAMEPLE_CNT = 100; 
    private Map<Integer, LoggerDto> cache;

    @Autowired
    private LoggerRepository loggerRepository;

    @PostConstruct
    private void init() {
        this.cache = loggerRepository.findAll().stream().collect(
                Collectors.toMap(Logger::getId, x -> x.toDto()));
    }

    public LoggerDto get(int id) {
        return this.cache.get(id);
    }
    
    public LoggerDto get(int id, String ip) {
        this.cache.get(id).setIp(ip);
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
            this.cache.put(logger.getId(), logger.toDto());
            res.setMsg(msg);
            res.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        res.setData(logger);
        return res;
    }

    public SingleObjectResponse remove(Logger logger) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        try {
            LoggerDto info = this.cache.get(logger.getId());
            res.setData(info.toEntity());
            String msg = "logger was removed. " + info.getName();
            res.setMsg(msg);
            loggerRepository.deleteById(logger.getId());
            this.cache.remove(logger.getId());
        } catch (Exception e) {
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    
    public List<Map<String, Object>> sample(Logger logger) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LoggerDto dto =  logger.toDto();
            for (int i = 0; i < SAMEPLE_CNT; i++) {
                list.add(dto.getDetail().generateLog());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Map<String, Object> map = new HashMap<>();
            map.put("error", sw.toString());
            list.add(map);
        }
        return list;
    }
}
