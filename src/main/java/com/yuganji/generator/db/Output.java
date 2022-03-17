package com.yuganji.generator.db;

import com.yuganji.generator.output.model.OutputDto;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String type;

    @Builder.Default
    @Column(nullable = false)
    private Long created = System.currentTimeMillis();

    @Builder.Default
    @Column(name = "last_modified", nullable = false, updatable = false)
    private Long lastModified = System.currentTimeMillis();

    @Column(nullable = false, columnDefinition = "json")
    @Convert(converter = OutputInfoConverter.class)
    private Map<String, Object> info;

    @Column(name = "max_queue_size", nullable = false)
    @ColumnDefault(value = "100000")
    private int maxQueueSize;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault(value = "0")
    private Integer status = 0;

    public static OutputBuilder builder(OutputDto outputDto) {
//        ObjectMapper om = new ObjectMapper()
//                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
//        String info = null;
//        try {
//            info = om.writeValueAsString(outputDto.getInfo());
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
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
