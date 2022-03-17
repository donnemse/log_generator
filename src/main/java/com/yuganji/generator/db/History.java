package com.yuganji.generator.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.yuganji.generator.model.HistoryDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "history")
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int fid;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String ip;

    @Builder.Default
    @Column(name = "last_modified", nullable = false)
    private long lastModified = System.currentTimeMillis();
    
    @Transient
    private String name;
    
    private String msg;

    private String detail;

    private String error;

    public HistoryDto toDto(){
        return HistoryDto.builder()
                .id(this.id)
                .fid(this.fid)
                .name(null)
                .type(this.type)
                .ip(this.ip)
                .lastModified(this.lastModified)
                .msg(this.msg)
                .detail(this.detail)
                .error(this.error)
                .build();
    }
}
