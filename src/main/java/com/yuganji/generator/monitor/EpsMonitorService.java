package com.yuganji.generator.monitor;

import com.yuganji.generator.kafka.KafkaProducerService;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.model.LoggerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map.Entry;

@Service
public class EpsMonitorService {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private LoggerService loggerService;

    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
    public void monitorEps() {
        long time = System.currentTimeMillis();

        Iterator<Entry<Integer, EpsVO>> it = kafkaProducerService.getProducerEps().entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, EpsVO> entry = it.next();
            int loggerId = entry.getKey();
            EpsVO eps = entry.getValue();

            LoggerDto logger = loggerService.get(loggerId);
            if (logger == null || logger.getStatus() == 0) {
                it.remove();
                continue;
            }
            if (time - eps.getLastCheckTime() > 1000L) {
                eps.setEps(time);
            }
            logger.setProducerEps(eps);
        }
    }
}
