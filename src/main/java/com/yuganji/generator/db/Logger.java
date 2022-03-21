package com.yuganji.generator.db;

import com.yuganji.generator.model.LoggerDto;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Data
@Entity
@Table(name = "logger")
@AllArgsConstructor
@Builder(builderMethodName = "LoggerBuilder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Logger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    private String name;
    
    @Column(nullable = false)
    private String eps;

    @Column(name = "yaml_str", nullable = false)
    private String yamlStr;

    @Column(nullable = false)
    private String ip;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Long created = System.currentTimeMillis();

    @Builder.Default
    @Column(name = "last_modified", nullable = false)
    private Long lastModified = System.currentTimeMillis();

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault(value = "0")
    private Integer status = 0;

    public static LoggerBuilder builder(LoggerDto loggerDto) {
        return LoggerBuilder()
                .id(loggerDto.getId())
                .name(loggerDto.getName())
                .eps(loggerDto.getEps())
                .yamlStr(loggerDto.getYamlStr())
                .ip(loggerDto.getIp())
                .created(loggerDto.getCreated())
                .lastModified(loggerDto.getLastModified())
                .status(loggerDto.getStatus());
    }

    public LoggerDto toDto() {
        return LoggerDto.builder()
                .id(this.id)
                .name(this.name)
                .eps(this.eps)
                .yamlStr(this.yamlStr)
                .ip(this.ip)
                .created(this.created)
                .lastModified(this.lastModified)
                .status(this.status).build();
    }
}
