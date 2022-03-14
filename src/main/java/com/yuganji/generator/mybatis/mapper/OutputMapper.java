package com.yuganji.generator.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yuganji.generator.output.model.Output;

@Mapper
public interface OutputMapper {
    List<Output> listOutput();
//    int insertHistory(int outputId, String type, String ip, long lastModified, String msg, String etc);
}