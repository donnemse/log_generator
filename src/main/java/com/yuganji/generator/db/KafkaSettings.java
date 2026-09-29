package com.yuganji.generator.db;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Table(name = "kafka_settings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaSettings {

    @Id
    @Column(nullable = false)
    private Integer id;  // Always 1 (single-row config)

    @Column(name = "bootstrap_servers")
    private String bootstrapServers;

    @Builder.Default
    @Column(name = "output_type")
    private String outputType = "json";  // "json" or "csv"

    @Builder.Default
    @Column(name = "batch_size")
    private Integer batchSize = 1000;

    @Builder.Default
    @Column(name = "output_target")
    private String outputTarget = "kafka";  // "kafka" or "file"

    @Builder.Default
    @Column(name = "output_dir")
    private String outputDir = "./output";  // used when outputTarget == "file"

    @Builder.Default
    @Column(name = "output_file")
    private String outputFile = "output.jsonl";  // used when outputTarget == "file"

}
