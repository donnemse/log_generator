package com.yuganji.generator.output.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.yuganji.generator.db.Output;
import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.output.sparrow.ISocketServer;
import com.yuganji.generator.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Log4j2
public class OutputDto {
    @JsonIgnore
    transient private static final ObjectMapper om = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    @JsonIgnore
    transient private static final int MAX_QUEUE_SIZE = 10_000;
    private int id;
    private String name;
    private String ip;
    private List<String> clients;

    @Builder.Default
    private Map<Integer, EpsVO> producerEps = new ConcurrentHashMap<>();;
    private EpsVO consumerEps;

    private int maxQueueSize;
    private int currentQueueSize;
    private long currentQueueByte;
    private long startedTime;
    private long runningTime;
    private long created;
    private long lastModified;

    @Builder.Default
    @JsonIgnore
    private transient LinkedBlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

    @JsonIgnore
    transient private AbstractOutputHandler handler;

    private Map<String, Object> info;

    private String type;
    private int status;

    public void setInfo(Object info) {
        try {
            Map<String, Object> map = null;
            if (info == null) {
                map = new HashMap<>();
            } else if (info instanceof String) {
                map = om.readValue(info.toString(), new TypeReference<Map<String, Object>>() {
                });
            } else if (info instanceof Map<?, ?>) {
                map = (Map<String, Object>) info;
            }
            if (this.type.equalsIgnoreCase(Constants.Output.SPARROW.getValue())) {
                this.handler = new SparrowOutput(this.id, map);
            } else if (this.type.equalsIgnoreCase(Constants.Output.FILE.getValue())) {
                this.handler = new FileOutput(this.id, map);
            }
            this.info = map;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            this.info = null;
        }
    }
    
    public void resetHandler() {
        if (this.type.equalsIgnoreCase(Constants.Output.SPARROW.getValue())) {
            this.handler = new SparrowOutput(this.id, this.info);
        } else if (this.type.equalsIgnoreCase(Constants.Output.FILE.getValue())) {
            this.handler = new FileOutput(this.id, this.info);
        }
    }
    
    private void initialize(int maxQueueSize) {

    }
    
    public long getRunningTime() {
        this.runningTime = System.currentTimeMillis() - this.startedTime;
        return runningTime;
    }

    @JsonIgnore
    public List<String> getClients(){
        if (!this.type.equals(Constants.Output.SPARROW.getValue())) {
            return null;
        }
        if (this.handler == null){
            return null;
        }
        ISocketServer server = ((SparrowOutput) this.handler).getServer();
        
        if (server == null) {
            this.clients = new ArrayList<>();
        } else {
            this.clients = new ArrayList<>(server.getClients().keySet());
        }
        return this.clients;
    }

    public Output toEntity() {
        return Output.builder(this).build();
    }

    public static class OutputDtoBuilder {

        public OutputDtoBuilder maxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            this.queue(new LinkedBlockingQueue<>(maxQueueSize));
            return this;
        }

        public OutputDtoBuilder info(Object info) {
            try {
                Map<String, Object> map = null;
                if (info == null) {
                    map = new HashMap<>();
                } else if (info instanceof String) {
                    map = om.readValue(info.toString(), new TypeReference<Map<String, Object>>() {
                    });
                } else if (info instanceof Map<?, ?>) {
                    map = (Map<String, Object>) info;
                }

                if (this.type.equalsIgnoreCase(Constants.Output.SPARROW.getValue())) {
                    this.handler = new SparrowOutput(this.id, map);
                } else if (this.type.equalsIgnoreCase(Constants.Output.FILE.getValue())) {
                    this.handler = new FileOutput(this.id, map);
                }
                this.info = map;
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                this.info = null;
            }
            return this;
        }
    }
}
