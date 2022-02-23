package com.igloosec.generator.history;

import java.util.List;

import lombok.Data;

@Data
public class HistoryResponse {
    private final int pageSize = 10;
    
    private int totalPage;
    private int currPage;
    private int totalCnt;
    private int offset;
    private int limit;
    private String type;
    private List<HistoryVO> list;
    
    public HistoryResponse(String type, int currPage) {
        this.type = type;
        this.currPage = currPage;
        this.offset = (currPage - 1) * pageSize;
        this.limit = pageSize;
    }
    
    public void setTotalCnt(int totalCnt) {
        this.totalCnt = totalCnt;
        this.totalPage = (totalCnt + pageSize) / pageSize; 
    }
    
}
