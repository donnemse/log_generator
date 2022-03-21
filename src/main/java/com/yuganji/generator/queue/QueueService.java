package com.yuganji.generator.queue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.EpsVO;

import lombok.Getter;

@Service
public class QueueService {

    @JsonIgnore
    transient private static final int MAX_QUEUE_SIZE = 10_000;

    @Getter
    private transient Map<Integer, QueueObject> queue;

    @Autowired
    private LoggerService loggerService;

    @PostConstruct
    public void init() {
        this.queue = new LinkedHashMap<>();
    }

    public void push(Map<String, Object> data, int loggerId) {
        this.queue.entrySet().parallelStream().forEach(entry -> {
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
}
