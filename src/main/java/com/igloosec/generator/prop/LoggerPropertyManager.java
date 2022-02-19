package com.igloosec.generator.prop;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.generator.restful.model.LoggerYamlVO;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class LoggerPropertyManager {
    
    private Map<String, LoggerProperty> cache;
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    
    @PostConstruct
    private void init() {
        this.cache = new HashMap<>();
        
        
        List<File> files = (List<File>) FileUtils.listFiles(new File("./config"), new String[] {"yaml"}, true);
        for (File f: files) {
            try {
                LoggerProperty lp = mapper.readValue(f, LoggerProperty.class);
                this.cache.put(f.getName(), lp);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * @param name
     * @param yaml
     * @return
     */
    public boolean createLogger(LoggerYamlVO vo) {
        try {
            LoggerProperty lp = mapper.readValue(vo.getYaml(), LoggerProperty.class);
            // TODO validateCheck
            this.cache.put(vo.getFileName(), lp);
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
    public boolean modifyLogger(LoggerYamlVO vo) {
        try {
            // TODO Stop logging
            LoggerProperty lp = mapper.readValue(vo.getYaml(), LoggerProperty.class);
            // TODO validateCheck
            this.cache.remove(vo.getFileName());
            this.cache.put(vo.getNewFileName(), lp);
            
            // TODO write File
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
    public boolean deleteLogger(LoggerYamlVO vo) {
        try {
            // TODO Stop logging
            // TODO validateCheck
            this.cache.remove(vo.getFileName());
            
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
    
//    LogProperty a = mapper.readValue(new File("config/apache.yaml"), LogProperty.class);
//    while(true) {
//        Map<String, Object> b = a.generateLog();
//        queueService.pushLog(b);
//        log.debug(b);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//} catch (IOException e1) {
//    // TODO Auto-generated catch block
//    e1.printStackTrace();
//}
}
