package com.igloosec.generator.monitor;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.igloosec.generator.model.EpsVO;
import com.igloosec.generator.model.OutputInfoVO;
import com.igloosec.generator.output.OutputService;
import com.igloosec.generator.util.CommonUtil;

@Service
public class EpsMonitorService {
    
    @Autowired
    private OutputService outputService;
    
    @Scheduled(initialDelay = 3000, fixedDelay = 3000)
    public void monitorEps() {
        long time = System.currentTimeMillis();
        for (Entry<Integer, OutputInfoVO> entryInfo: outputService.getCache().entrySet()){
            for (Entry<Integer, EpsVO> entryEps: entryInfo.getValue().getProducerEps().entrySet()){
                long diff = time - entryEps.getValue().getLastCheckTime();
                entryEps.getValue().setEps(Math.ceil(entryEps.getValue().getCnt() / Math.floor(diff / 1000.d)));
                entryEps.getValue().setDeletedEps(Math.ceil(entryEps.getValue().getDeleted() / Math.floor(diff / 1000.d)));
                entryEps.getValue().setCnt(0);
                entryEps.getValue().setDeleted(0);
                entryEps.getValue().setLastCheckTime(time);
            }
            if (entryInfo.getValue().getConsumerEps() != null) {
                long diff = time - entryInfo.getValue().getConsumerEps().getLastCheckTime();
                entryInfo.getValue().getConsumerEps().setEps(
                        entryInfo.getValue().getConsumerEps().getCnt() / (diff / 1000.d));
                entryInfo.getValue().getConsumerEps().setCnt(0);
                entryInfo.getValue().getConsumerEps().setLastCheckTime (time);
            }
            entryInfo.getValue().setCurrentQueueSize(entryInfo.getValue().getQueue().size());
            entryInfo.getValue().setCurrentQueueByte(
                    CommonUtil.calcObjectSize(entryInfo.getValue().getQueue()));
        }
        
        
    }
    
//    @Scheduled(fixedDelay = 15 * 1000)
//    public void schedule() {
//        
//        for (Entry<Integer, LinkedBlockingQueue<Map<String, Object>>> entry: queueService.getQueue().entrySet()) {
//            if(this.outputService.getCache().containsKey(entry.getKey())) {
//                int size = entry.getValue().size();
//                this.outputService.get(entry.getKey()).setCurrentQueueSize(size);
//                this.outputService.get(entry.getKey()).setMaxQueueSize(size + entry.getValue().remainingCapacity());
//                this.outputService.get(entry.getKey()).setProducerEps(queueService.getProducerEpsCache().get(entry.getKey()));
//                this.outputService.get(entry.getKey()).setConsumerEps(queueService.getConsumerEpsCache().get(entry.getKey()));
//            }
//        }
//    }
    
}
