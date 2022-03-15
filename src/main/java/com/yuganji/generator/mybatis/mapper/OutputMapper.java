package com.yuganji.generator.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.yuganji.generator.output.model.Output;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutputMapper {
    List<Output> listOutput();

    int insertOutput(Output output);
    int updateOutput(Output output);
    int updateOutputStatus(
            @Param("id") int id,
            @Param("status") int status);
    void removeOutput(
            @Param("id") int id);
//    int insertHistory(int outputId, String type, String ip, long lastModified, String msg, String etc);
}