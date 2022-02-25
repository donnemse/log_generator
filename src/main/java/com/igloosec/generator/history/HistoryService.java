package com.igloosec.generator.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.mybatis.mapper.HistoryMapper;
import com.igloosec.generator.prop.LoggerPropertyManager;

@Service
public class HistoryService {
    @Autowired
    private HistoryMapper mapper;
    
    @Autowired
    private LoggerPropertyManager loggerPropMng;
    
    public HistoryResponse list(int page) {
        HistoryResponse res = new HistoryResponse(page);
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
