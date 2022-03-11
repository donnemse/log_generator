package com.yuganji.generator.history;

import com.yuganji.generator.mybatis.mapper.HistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yuganji.generator.logger.LoggerManager;
import com.yuganji.generator.model.HistoryResponseVO;
import com.yuganji.generator.model.HistoryVO;

@Service
public class HistoryService {
    @Autowired
    private HistoryMapper mapper;
    
    @Autowired
    private LoggerManager loggerPropMng;
    
    public HistoryResponseVO list(int page) {
        HistoryResponseVO res = new HistoryResponseVO(page);
        res.setTotalCnt(mapper.totalCnt());
        res.setList(mapper.list(res));
        
        for (HistoryVO vo: res.getList()) {
            if (vo.getType().equals("logger")) {
                if (loggerPropMng.getLogger(vo.getFid()) != null) {
                    vo.setName(loggerPropMng.getLogger(vo.getFid()).getName());
                }
            }
        }
        return res;
    }

}
