package com.igloosec.generator.prop;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.generator.mybatis.mapper.LoggerMapper;
import com.igloosec.generator.restful.model.LoggerRequestVO;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LoggerPropertyManager {
    private final int SAMEPLE_CNT = 100; 
    private Map<Integer, LoggerPropertyInfo> cache;
    private ObjectMapper om = new ObjectMapper(new YAMLFactory());
    
    @Autowired
    private LoggerMapper mapper;
    
    @PostConstruct
    private void init() {
        this.cache = new HashMap<>();
        List<LoggerPropertyInfo> listInfo = this.mapper.listLogger();
        this.cache = listInfo.stream()
            .collect(Collectors.toMap(LoggerPropertyInfo::getId, x -> {
                try {
                    LoggerProperty lp = om.readValue(x.getYamlStr(), LoggerProperty.class);
                    x.setLogger(lp);
                } catch (JsonMappingException e) {
                    log.error(e.getMessage(), e);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage(), e);
                }
                return x;
            }));
        
//        List<File> files = (List<File>) FileUtils.listFiles(configDir, new String[] {"yaml"}, true);
//        for (File f: files) {
//            try {
//                LoggerProperty lp = om.readValue(f, LoggerProperty.class);
//                
//                LoggerPropertyInfo info = null;
//                if (mapInfo.containsKey(f.getName())) {
//                    info = mapInfo.get(f.getName());
//                } else {
//                    info = this.createLogger(f);
//                }
//                info.setLogger(lp);
//                this.cache.put(info.getId(), info);
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//            }
//        }
    }
    
    private LoggerPropertyInfo createLogger(File f) throws Exception {
        LoggerPropertyInfo info = new LoggerPropertyInfo();
        
        info.setName(f.getName());
        info.setCreated(new Date().getTime());
        info.setLastModified(new Date().getTime());
        info.setIp("System");
        info.setStatus(0);
        info.setYamlStr(FileUtils.readFileToString(f, Charset.defaultCharset()));
        int i = mapper.insertLogger(info);
        log.debug("********************");
        log.debug(info);
        if (i == 0) {
            throw new Exception("can not insert logger");
        }
        return info;
    }

    public LoggerPropertyInfo getLogger(int id) {
        return this.cache.get(id);
    }
    
    public Map<Integer, LoggerPropertyInfo> listLogger() {
        return this.cache;
    }
    /**
     * @param name
     * @param yaml
     * @return
     */
    public boolean createLogger(LoggerRequestVO vo) {
        try {
            LoggerProperty lp = om.readValue(vo.getYaml(), LoggerProperty.class);
            LoggerPropertyInfo info = new LoggerPropertyInfo();
            info.setLogger(lp);
            info.setName(vo.getName());
            
            // TODO validateCheck
            this.cache.put(info.getId(), info);
            // TODO write File
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
    /**
     * @param name
     * @param yaml
     * @return
     */
    public boolean modifyLogger(LoggerRequestVO vo) {
        try {
            // TODO Stop logging
            LoggerProperty lp = om.readValue(vo.getYaml(), LoggerProperty.class);
            // TODO validateCheck
            this.cache.remove(vo.getId());
            LoggerPropertyInfo info = new LoggerPropertyInfo();
            info.setLogger(lp);
            info.setId(vo.getId());
            info.setIp(vo.getIp());
            info.setName(vo.getName());
            info.setLastModified(new Date().getTime());
            info.setYamlStr(vo.getYaml());
            mapper.updateLogger(info);
            this.cache.put(vo.getId(), info);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
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
    
    @Async
    public void run() throws IOException, InterruptedException {
        log.debug("Start Log Property manager");
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get("./config");
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                log.debug("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    
                } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    this.cache.remove(event.context());
                }
                
            }
            log.debug(this.cache);
            key.reset();
        }
    }
    
    public List<Map<String, Object>> sample(LoggerRequestVO vo) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            LoggerProperty lp = om.readValue(vo.getYaml(), LoggerProperty.class);
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
