package com.igloosec.generator.prop;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.generator.mybatis.mapper.HistoryMapper;
import com.igloosec.generator.mybatis.mapper.LoggerMapper;
import com.igloosec.generator.model.LoggerPropVO;
import com.igloosec.generator.model.LoggerRequestVO;
import com.igloosec.generator.model.LoggerVO;
import com.igloosec.generator.model.SingleObjectResponse;

import lombok.extern.log4j.Log4j2;

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
    
    @PostConstruct
    private void init() {
        this.cache = new HashMap<>();
        List<LoggerVO> listInfo = this.loggerMapper.listLogger();
        this.cache = listInfo.stream()
            .collect(Collectors.toMap(LoggerVO::getId, x -> {
                try {
                    LoggerPropVO lp = om.readValue(x.getYamlStr(), LoggerPropVO.class);
                    x.setLogger(lp);
                } catch (JsonMappingException e) {
                    log.error(e.getMessage(), e);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage(), e);
                }
                return x;
            }));
    }
    
//    private LoggerPropertyInfo createLogger(File f) throws Exception {
//        LoggerPropertyInfo info = new LoggerPropertyInfo();
//        
//        info.setName(f.getName());
//        info.setCreated(new Date().getTime());
//        info.setLastModified(new Date().getTime());
//        info.setIp(NetUtil.getLocalHostIp());
//        info.setStatus(0);
//        info.setYamlStr(FileUtils.readFileToString(f, Charset.defaultCharset()));
//        int i = loggerMapper.insertLogger(info);
//        log.debug("********************");
//        log.debug(info);
//        if (i == 0) {
//            throw new Exception("can not insert logger");
//        }
//        return info;
//    }

    public LoggerVO getLogger(int id) {
        return this.cache.get(id);
    }
    
    public Map<Integer, LoggerVO> listLogger() {
        return this.cache;
    }
    /**
     * @param name
     * @param yaml
     * @return
     */
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
    
    public boolean addHistory(LoggerRequestVO vo, String msg, String detail, String error) {
        histMapper.insertHistory(vo.getId(), vo.getIp(), TYPE, new Date().getTime(), msg, detail, null);
        return true;
    }
    
    
    /**
     * @param name
     * @param yaml
     * @return
     */
    public SingleObjectResponse modifyLogger(LoggerRequestVO vo) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
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
            loggerMapper.updateLogger(info);
            this.addHistory(vo, "logger was modified. " + vo.getName(), vo.getYaml(), null);
            this.cache.put(vo.getId(), info);
            res.setMsg("logger was modified. " + vo.getName());
            res.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.addHistory(vo, "could not modified logger. " + vo.getName(), null, e.getMessage());
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public boolean deleteLogger(LoggerRequestVO vo) {
        try {
            // TODO Stop logging
            // TODO validateCheck
            this.cache.remove(vo.getId());
            
            // TODO remove File
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
//    @Async
//    public void run() throws IOException, InterruptedException {
//        log.debug("Start Log Property manager");
//        WatchService watchService = FileSystems.getDefault().newWatchService();
//        Path path = Paths.get("./config");
//        path.register(watchService,
//                StandardWatchEventKinds.ENTRY_CREATE,
//                StandardWatchEventKinds.ENTRY_DELETE,
//                StandardWatchEventKinds.ENTRY_MODIFY);
//
//        WatchKey key;
//        while ((key = watchService.take()) != null) {
//            for (WatchEvent<?> event : key.pollEvents()) {
//                log.debug("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
//                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
//                    
//                } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
//                    
//                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
//                    this.cache.remove(event.context());
//                }
//                
//            }
//            log.debug(this.cache);
//            key.reset();
//        }
//    }
    
    public List<Map<String, Object>> sample(LoggerRequestVO vo) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LoggerPropVO lp = om.readValue(vo.getYaml(), LoggerPropVO.class);
            IntStream.range(0, SAMEPLE_CNT).forEach(x ->{
                list.add(lp.generateLog());
            });
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Map<String, Object> map = new HashMap<>();
            map.put("error", sw.toString());
            list.add(map);
        }
        return list;
    }
}
