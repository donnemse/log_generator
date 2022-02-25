package com.igloosec.generator.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.igloosec.generator.history.HistoryResponse;
import com.igloosec.generator.history.HistoryVO;

@Mapper
public interface HistoryMapper {
//    int insertHistory(int outputId, String type, String ip, long lastModified, String msg);
    
    int totalCnt();
    
    List<HistoryVO> list(HistoryResponse vo);
    
    int insertHistory(int fid, String ip, String type, long lastModified, String msg, String detail, String error);
//    int insertHistory(HistoryVO vo);
}