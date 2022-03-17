package com.yuganji.generator.db;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "history")
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

    @Column(name = "last_modified", nullable = false)
    private long lastModified;

    private String msg;

    private String detail;

    private String error;
}
