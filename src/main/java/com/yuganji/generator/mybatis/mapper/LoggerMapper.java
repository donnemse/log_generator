package com.yuganji.generator.mybatis.mapper;

import com.yuganji.generator.model.LoggerDto;

import java.util.List;

//@Mapper
public interface LoggerMapper {
    List<LoggerDto> listLogger();

    int insertLogger(LoggerDto info);

    int updateLogger(LoggerDto info);

//    int updateLoggerStatus(
//            @Param("id") int id,
//            @Param("status") int status);
//
//    void removeLogger(
//            @Param("id") int id);
}