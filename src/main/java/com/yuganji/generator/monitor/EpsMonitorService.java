package com.yuganji.generator.monitor;

import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.output.model.OutputDto;
import com.yuganji.generator.queue.QueueObject;
import com.yuganji.generator.queue.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map.Entry;

@Service
public class EpsMonitorService {
    
    @Autowired
    private QueueService queueService;
    
    @Autowired
    private OutputService outputService;
    
    @Autowired
    private LoggerService loggerService;
    
    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
    public void monitorEps() {
        long time = System.currentTimeMillis();
        for (Entry<Integer, QueueObject> entryInfo: queueService.entry()){
            java.util.Iterator<Entry<Integer, EpsVO>> it = entryInfo.getValue().getProducerEps().entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, EpsVO> entryEps = it.next();
                LoggerDto logger = loggerService.get(entryEps.getKey());
                if (logger == null || logger.getStatus() == 0) {
                    it.remove();
                    continue;
                }
                if (time - entryEps.getValue().getLastCheckTime() > 1000L) {
                    entryEps.getValue().setEps(time);
                }
            }
            if (entryInfo.getValue().getConsumerEps() != null) {
                if (time - entryInfo.getValue().getConsumerEps().getLastCheckTime() > 1000L) {
                    entryInfo.getValue().getConsumerEps().setEps(time);
                }
            }
            OutputDto output = outputService.get(entryInfo.getKey());
            
            output.setCurrentQueueSize(entryInfo.getValue().getQueue().size());
            output.setCurrentQueueByte(
                    (int) entryInfo.getValue().getTotalBytes().get());
            output.setProducerEps(entryInfo.getValue().getProducerEps());
            
            if (output.getStatus() == 1) {
                output.setConsumerEps(entryInfo.getValue().getConsumerEps());
            } else {
                output.setConsumerEps(null);
            }
        }
    }
}
