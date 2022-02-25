package com.igloosec.generator.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.igloosec.generator.model.LoggerVO;

@Mapper
public interface LoggerMapper {
    List<LoggerVO> listLogger();

    int insertLogger(LoggerVO info);

    int updateLogger(LoggerVO info);

    int updateLoggerStatus(int id, int status);
}