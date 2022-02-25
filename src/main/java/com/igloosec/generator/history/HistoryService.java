package com.igloosec.generator.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.logger.LoggerManager;
import com.igloosec.generator.model.HistoryResponseVO;
import com.igloosec.generator.model.HistoryVO;
import com.igloosec.generator.mybatis.mapper.HistoryMapper;

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
