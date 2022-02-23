package com.igloosec.generator.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloosec.generator.mybatis.mapper.HistoryMapper;

@Service
public class HistoryService {
    @Autowired
    private HistoryMapper mapper;
    public HistoryResponse list(String type, int page) {
        HistoryResponse res = new HistoryResponse(type, page);
        res.setTotalCnt(mapper.totalCnt(type));
        res.setList(mapper.list(res));
        return res;
    }

}
