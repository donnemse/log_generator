package com.yuganji.generator.output.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuganji.generator.model.AbstractOutputHandler;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.output.sparrow.ISocketServer;
import com.yuganji.generator.util.Constants;
import com.yuganji.generator.util.NetUtil;

import lombok.Data;

@Data
public class Output {
    
    transient private static final ObjectMapper om = new ObjectMapper(); 
    
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
    private transient LinkedBlockingQueue<Map<String, Object>> queue;
    
    transient private AbstractOutputHandler handler;
    private Map<String, Object> info;
    
    private String type;
    private int status;
    
    public Output() {
        this.initialize(MAX_QUEUE_SIZE);
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }
    public void setInfo(String info) {
        try {
            Map<String, Object> map = om.readValue(info, new TypeReference<Map<String, Object>>() {
            });
            if (this.info == null) {
                if (this.type.equalsIgnoreCase(Constants.Output.SPARROW.getValue())) {
                    this.handler = new SparrowOutput(this.id, map);
                } else if (this.type.equalsIgnoreCase(Constants.Output.FILE.getValue())) {
                    this.handler = new FileOutput(this.id, map);
                }
            }
            this.info = map;
        } catch (JsonProcessingException e) {
            this.info = null;
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
    
    public List<String> getClients(){
        if (!this.type.equals(Constants.Output.SPARROW.getValue())) {
            return null;
        }
        ISocketServer server = ((SparrowOutput) this.info).getServer();
        
        if (server == null) {
            this.clients = new ArrayList<>();
        } else {
            this.clients = new ArrayList<String>(server.getClients().keySet());
        }
        return this.clients;
    }
}
