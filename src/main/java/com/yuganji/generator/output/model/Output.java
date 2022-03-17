package com.yuganji.generator.output.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.output.sparrow.ISocketServer;
import com.yuganji.generator.util.Constants;
import com.yuganji.generator.util.NetUtil;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Data
@Log4j2
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Output {
    @JsonIgnore
    transient private static final ObjectMapper om = new ObjectMapper();

    @JsonIgnore
    transient private static final int MAX_QUEUE_SIZE = 10_000;
    private int id;
    private String name;
    private String ip;
    private List<String> clients;
    private Map<Integer, EpsVO> producerEps;
    private EpsVO consumerEps;

    private int maxQueueSize;
    private int currentQueueSize;
    private long currentQueueByte;
    private long startedTime;
    private long runningTime;
    private long created;
    private long lastModified;

    @JsonIgnore
    private transient LinkedBlockingQueue<Map<String, Object>> queue;

    @JsonIgnore
    transient private AbstractOutputHandler handler;

    private Map<String, Object> info;

    private String type = "sparrow";
    private int status;
    
    public Output() {
        this.initialize(MAX_QUEUE_SIZE);
    }

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
        this.startedTime = System.currentTimeMillis();
        this.ip = NetUtil.getLocalHostIp();
        this.maxQueueSize = maxQueueSize;
        this.queue = new LinkedBlockingQueue<>(this.maxQueueSize);
        this.producerEps = new ConcurrentHashMap<>();
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
}
