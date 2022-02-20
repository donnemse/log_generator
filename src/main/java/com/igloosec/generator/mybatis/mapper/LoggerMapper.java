package com.igloosec.generator.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoggerMapper {
    int selectNumber(@Param("number") int number);
}