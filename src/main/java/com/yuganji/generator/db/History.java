package com.yuganji.generator.db;

import com.yuganji.generator.model.HistoryDto;
import lombok.*;

import javax.persistence.*;

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
