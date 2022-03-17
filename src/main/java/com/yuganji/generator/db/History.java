package com.yuganji.generator.db;

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

    private String msg;

    private String detail;

    private String error;
}
