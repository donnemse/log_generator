package com.yuganji.generator.queue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.EpsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class QueueService {

    @JsonIgnore
    transient private static final int MAX_QUEUE_SIZE = 10_000;

    private transient Map<Integer, QueueObject> queue;

    @Autowired
    private LoggerService loggerService;

    @PostConstruct
    public void init() {
        this.queue = new LinkedHashMap<>();
    }

    public QueueObject getQueueObj(int outputId) {
        return this.queue.get(outputId);
    }

    public QueueObject removeQueueObj(int outputId) {
        return this.queue.remove(outputId);
    }

    public Set<Map.Entry<Integer, QueueObject>> entry(){
        return this.queue.entrySet();
    }

    public void push(Map<String, Object> data, int loggerId) {
        this.entry().parallelStream().forEach(entry -> {
            EpsVO eps = entry.getValue().getProducerEps().putIfAbsent(loggerId, new EpsVO(loggerService.get(loggerId).getName()));
            if (eps == null) {
                eps = entry.getValue().getProducerEps().get(loggerId);
            }
            
            if (entry.getValue().getQueue().remainingCapacity() == 0) {
                entry.getValue().getQueue().poll();
                eps.addDeleted();
            }
            entry.getValue().getQueue().offer(data);
            eps.addCnt();
        });
    }

    public List<Map<String, Object>> poll(int queueId, int maxBuffer) {
        EpsVO eps = this.queue.get(queueId).getConsumerEps();
        
        List<Map<String, Object>> list = new ArrayList<>();
        int cnt = this.queue.get(queueId).getQueue().drainTo(list, maxBuffer);
        eps.addCnt(cnt);
        return list;
    }

    public void removeProducerEps(int loggerId) {
        Set<Integer> set = new TreeSet<>(this.queue.keySet());
        for (int queueId: set) {
            this.queue.get(queueId).getProducerEps().remove(loggerId);
        }
    }

    public QueueObject putIfAbsent(Integer k, QueueObject queueObject) {
        return this.queue.putIfAbsent(k, queueObject);
    }
}
