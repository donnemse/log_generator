package com.yuganji.generator.history;

import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.HistoryDto;
import com.yuganji.generator.model.HistoryResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

    @Autowired
    private LoggerService loggerPropMng;
    
    public HistoryResponseVO list(int page) {
        HistoryResponseVO res = new HistoryResponseVO(page);
//        res.setTotalCnt(mapper.totalCnt());
//        res.setList(mapper.list(res));
        
        for (HistoryDto vo: res.getList()) {
            if (vo.getType().equals("logger")) {
                if (loggerPropMng.get(vo.getFid()) != null) {
                    vo.setName(loggerPropMng.get(vo.getFid()).getName());
                }
            }
        }
        return res;
    }

}
