package com.igloosec.generator.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutputMapper {
    int insertHistory(int outputId, String type, String ip, long lastModified, String msg, String etc);
}