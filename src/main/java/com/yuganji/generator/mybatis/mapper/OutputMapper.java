package com.yuganji.generator.mybatis.mapper;

import com.yuganji.generator.output.model.OutputDto;

import java.util.List;

//@Mapper
public interface OutputMapper {
    List<OutputDto> listOutput();

    int insertOutput(OutputDto outputDto);
    int updateOutput(OutputDto outputDto);
//    int updateOutputStatus(
//            @Param("id") int id,
//            @Param("status") int status);
//    void removeOutput(
//            @Param("id") int id);
//    int insertHistory(int outputId, String type, String ip, long lastModified, String msg, String etc);
}