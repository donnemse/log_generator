package com.yuganji.generator.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yuganji.generator.model.LoggerVO;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoggerMapper {
    List<LoggerVO> listLogger();

    int insertLogger(LoggerVO info);

    int updateLogger(LoggerVO info);

    int updateLoggerStatus(
            @Param("id") int id,
            @Param("status") int status);

    void removeLogger(
            @Param("id") int id);
}