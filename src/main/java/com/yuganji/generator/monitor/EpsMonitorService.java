package com.yuganji.generator.monitor;

import java.util.Map.Entry;

import com.yuganji.generator.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.model.OutputInfoVO;
import com.yuganji.generator.output.OutputService;

@Service
public class EpsMonitorService {
    
    @Autowired
    private OutputService outputService;
    
    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
    public void monitorEps() {
        long time = System.currentTimeMillis();
        for (Entry<Integer, OutputInfoVO> entryInfo: outputService.getCache().entrySet()){
            for (Entry<Integer, EpsVO> entryEps: entryInfo.getValue().getProducerEps().entrySet()){
                entryEps.getValue().setEps(time);
            }
            if (entryInfo.getValue().getConsumerEps() != null) {
                entryInfo.getValue().getConsumerEps().setEps(time);
            }
            entryInfo.getValue().setCurrentQueueSize(entryInfo.getValue().getQueue().size());
            entryInfo.getValue().setCurrentQueueByte(
                    CommonUtil.calcObjectSize(entryInfo.getValue().getQueue()));
        }
        
        
    }
}
