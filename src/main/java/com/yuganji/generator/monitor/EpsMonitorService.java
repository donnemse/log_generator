package com.yuganji.generator.monitor;

import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.output.OutputService;
import com.yuganji.generator.output.model.OutputDto;
import com.yuganji.generator.queue.QueueObject;
import com.yuganji.generator.queue.QueueService;
import com.yuganji.generator.util.CommonUtil;
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
            for (Entry<Integer, EpsVO> entryEps: entryInfo.getValue().getProducerEps().entrySet()){
                if (loggerService.get(entryEps.getKey()).getStatus() == 0) {
                    entryInfo.getValue().getProducerEps().remove(entryEps.getKey());
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
                    CommonUtil.calcObjectSize(entryInfo.getValue().getQueue()));
            output.setProducerEps(entryInfo.getValue().getProducerEps());
            
            if (output.getStatus() == 1) {
                output.setConsumerEps(entryInfo.getValue().getConsumerEps());
            } else {
                output.setConsumerEps(null);
            }
        }
    }
}
