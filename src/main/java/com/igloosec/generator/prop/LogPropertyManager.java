package com.igloosec.generator.prop;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class LogPropertyManager {
    
    private Map<String, LogProperty> cache;
    
    
    @PostConstruct
    private void init() throws IOException, InterruptedException {
        this.cache = new HashMap<>();
//        this.run();
    }
    
//    @Scheduled(initialDelay = 3 * 1000, fixedRate = 10 * 1000)
    @Async
    public void run() throws IOException, InterruptedException {
        System.out.println("Start Log Property manager");
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get("./config");
        path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
            }
            key.reset();
        }
    }
}
