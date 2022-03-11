package com.yuganji.generator.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yuganji.generator.model.HistoryResponseVO;
import com.yuganji.generator.model.HistoryVO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HistoryMapper {
//    int insertHistory(int outputId, String type, String ip, long lastModified, String msg);
    
    int totalCnt();
    
    List<HistoryVO> list(HistoryResponseVO vo);
    
    int insertHistory(
            @Param("fid") int fid,
            @Param("ip") String ip,
            @Param("type") String type,
            @Param("lastModified") long lastModified,
            @Param("msg") String msg,
            @Param("detail") String detail,
            @Param("error") String error);
//    int insertHistory(HistoryVO vo);
}