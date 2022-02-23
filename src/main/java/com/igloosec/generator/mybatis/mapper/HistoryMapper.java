package com.igloosec.generator.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.igloosec.generator.history.HistoryResponse;
import com.igloosec.generator.history.HistoryVO;

@Mapper
public interface HistoryMapper {
    int insertHistory(int outputId, String type, String ip, long lastModified, String msg);
    
    int totalCnt(String type);
    
    List<HistoryVO> list(HistoryResponse vo);
}