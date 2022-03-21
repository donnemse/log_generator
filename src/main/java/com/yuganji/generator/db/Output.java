package com.yuganji.generator.db;

import com.yuganji.generator.output.model.OutputDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.Map;

@Data
@Entity
@Table(name = "output")
@AllArgsConstructor
@Builder(builderMethodName = "OutputBuilder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Output {

    @ApiModelProperty(value = "Output Id", dataType = "int", example = "0", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ApiModelProperty(value = "Output Name", dataType = "string", example = "Output Name")
    @Column(nullable = false)
    private String name;

    @ApiModelProperty(value = "Ip", dataType = "string", example = "192.168.0.1", hidden = true)
    @Column(nullable = false)
    private String ip;

    @ApiModelProperty(value = "Output Type", dataType = "string", example = "sparrow", allowableValues = "sparrow, file, kafka")
    @Column(nullable = false)
    private String type;

    @ApiModelProperty(value = "created time", dataType = "long", hidden = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Builder.Default
    @Column(nullable = false)
    private Long created = System.currentTimeMillis();

    @ApiModelProperty(value = "last modified time", dataType = "long", hidden = true,
            accessMode = ApiModelProperty.AccessMode.READ_ONLY, example = "0")
    @Builder.Default
    @Column(name = "last_modified", nullable = false, updatable = false)
    private Long lastModified = System.currentTimeMillis();

    @ApiModelProperty(value = "detail information by type", dataType = "json", example = "{\"port\": 3303}")
    @Column(nullable = false, columnDefinition = "json")
    @Convert(converter = OutputInfoConverter.class)
    private Map<String, Object> info;

    @ApiModelProperty(value = "max queue size", dataType = "int", example = "100000")
    @Column(name = "max_queue_size", nullable = false)
    @ColumnDefault(value = "100000")
    private int maxQueueSize;

    @ApiModelProperty(value = "Status", dataType = "int", hidden = true, accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault(value = "0")
    private Integer status = 0;

    public static OutputBuilder builder(OutputDto outputDto) {
        return OutputBuilder()
                .id(outputDto.getId())
                .name(outputDto.getName())
                .ip(outputDto.getIp())
                .type(outputDto.getType())
                .created(outputDto.getCreated())
                .lastModified(outputDto.getLastModified())
                .status(outputDto.getStatus())
                .info(outputDto.getInfo())
                .maxQueueSize(outputDto.getMaxQueueSize());
    }

    public OutputDto toDto() {
        return OutputDto.builder()
                .id(this.id)
                .name(this.name)
                .ip(this.ip)
                .type(this.type)
                .created(this.created)
                .lastModified(this.lastModified)
                .status(this.status)
                .info(this.info)
                .maxQueueSize(this.maxQueueSize).build();
    }
}
