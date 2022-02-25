package com.igloosec.generator.queue;

import org.springframework.stereotype.Service;

@Service
public class QueueService {
    private final int DEFAULT_QUEUE_MAX = 10_000;
    
//    @Getter
//    private Map<Integer, Map<Integer, EpsVO>> producerEpsCache;
//    
//    @Getter
//    private Map<Integer, EpsVO> consumerEpsCache;
    
//    @Autowired
//    private LoggerManager loggerMgr;
//    
//    
//    @PostConstruct
//    private void init() {
//        this.queue = new HashMap<>();
//        this.producerEpsCache = new ConcurrentHashMap<>();
//        this.consumerEpsCache = new ConcurrentHashMap<>();
//    }
//    
//    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
//    public void monitorEps() {
//        long time = System.currentTimeMillis();
//        
//        for (Entry<Integer, Map<Integer, EpsVO>> entryProducer: producerEpsCache.entrySet()){
//            for (Entry<Integer, EpsVO> entryEps: entryProducer.getValue().entrySet()){
//                long diff = time - entryEps.getValue().getLastCheckTime();
//                entryEps.getValue().setEps(Math.ceil(entryEps.getValue().getCnt() / Math.floor(diff / 1000.d)));
//                entryEps.getValue().setDeletedEps(Math.ceil(entryEps.getValue().getDeleted() / Math.floor(diff / 1000.d)));
//                entryEps.getValue().setCnt(0);
//                entryEps.getValue().setDeleted(0);
//                entryEps.getValue().setLastCheckTime(time);
//            }
//        }
//    }
//    
//    public void newQueue(int port, int queueSize) {
//        if (queueSize == 0) {
//            queueSize = DEFAULT_QUEUE_MAX;
//        }
//        this.queue.put(port, new LinkedBlockingQueue<>(queueSize));
//    }
//    
//    public void removeProducerEps(int loggerId) {
//        Set<Integer> set = new TreeSet<>(producerEpsCache.keySet());
//        for (int port: set) {
//            if (this.producerEpsCache.get(port).containsKey(loggerId)) {
//                this.producerEpsCache.get(port).remove(loggerId);
//            }
//        }
//    }
//    
//    public void removeQueue(int port) {
//        this.queue.remove(port);
//    }
//    
//    public Queue<Map<String, Object>> getQueue(int port, int queueSize) {
//        if (!this.queue.containsKey(port)) {
//            this.newQueue(port, queueSize);
//        }
//        return this.queue.get(port);
//    }
    
    
}
