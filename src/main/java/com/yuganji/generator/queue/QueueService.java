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

    private EpsVO consumerEps;

    @Autowired
    private LoggerService loggerService;

    @PostConstruct
    public void init() {
        this.queue = new LinkedHashMap<>();
    }

    public void push(Map<String, Object> data, int loggerId) {

        this.queue.entrySet().parallelStream().forEach(entry -> {
            if (!producerEps.containsKey(loggerId)) {
                EpsVO epsVO = new EpsVO(loggerService.get(loggerId).getName());
                this.producerEps.put(loggerId, epsVO);
            }
            if (entry.getValue().remainingCapacity() == 0) {
                entry.getValue().poll();
                this.producerEps.get(loggerId).addDeleted();
            }
            entry.getValue().offer(data);
            if (producerEps.get(loggerId) != null) {
                producerEps.get(loggerId).addCnt();
            }
        });
    }

    public List<Map<String, Object>> poll(int queueId, int maxBuffer) {
        if (this.queue.get(queueId).getConsumerEps() == null) {
            EpsVO epsVO = new EpsVO(null);
            this.cache.get(queueId).setConsumerEps(epsVO);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        int cnt = this.cache.get(queueId).getQueue().drainTo(list, maxBuffer);
        this.cache.get(queueId).getConsumerEps().addCnt(cnt);
        return list;
    }

    public void removeProducerEps(int loggerId) {
        Set<Integer> set = new TreeSet<>(this.cache.keySet());
        for (int queueId: set) {
            this.cache.get(queueId).getProducerEps().remove(loggerId);
        }
    }
}
