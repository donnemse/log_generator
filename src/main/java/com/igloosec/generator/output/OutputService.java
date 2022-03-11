package com.igloosec.generator.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.igloosec.generator.logger.LoggerManager;
import com.igloosec.generator.model.EpsHistoryVO;
import com.igloosec.generator.model.EpsVO;
import com.igloosec.generator.model.OutputInfoVO;
import com.igloosec.generator.model.SingleObjectResponse;
import com.igloosec.generator.mybatis.mapper.HistoryMapper;

import lombok.Getter;

@Service
public class OutputService {
    private static final String TYPE = "output";
    
    @Getter
    private Map<Integer, OutputInfoVO> cache;
    
    @Autowired
    private HistoryMapper histMapper;
    
    @Autowired
    private LoggerManager loggerMgr;
    
    @PostConstruct
    public void init() {
        this.cache = new ConcurrentHashMap<>();
    }
    
    public OutputInfoVO get(int id) {
        return this.cache.get(id);
    }
    
    public SingleObjectResponse open(OutputInfoVO vo) {
        
        if (cache.containsKey(vo.getPort())) {
            String message = "Already opened " + vo.getPort() + " port.";
            histMapper.insertHistory(vo.getPort(), vo.getOpenedIp(), TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
        } else {
            vo.setServer(new TCPSocketServer(vo.getPort(), this));
            vo.getServer().startServer();
            this.cache.put(vo.getPort(), vo);
            String message = "Successfully opened " + vo.getPort() + " port.";
            histMapper.insertHistory(vo.getPort(), vo.getOpenedIp(), TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(HttpStatus.OK.value(), message);
        }
    }

    public SingleObjectResponse close(int port, String ip) {
        if (cache.containsKey(port)) {
            cache.get(port).getServer().stopServer();
            cache.remove(port);
//            queueService.removeQueue(port);
            String message = "Sucessfully closed " + port + " port.";
            histMapper.insertHistory(port, ip, TYPE, new Date().getTime(), message, null, null);
            return new SingleObjectResponse(HttpStatus.OK.value(), message);
        }
        String message = "Could not close " + port + " port.";
        histMapper.insertHistory(port,ip, TYPE, new Date().getTime(), message, null, null);
        return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }

    public Collection<OutputInfoVO> list() {
        return this.cache.values();
    }
    
    public SingleObjectResponse stopClient(int port, String id) {
        if (!this.cache.containsKey(port) ||
                this.cache.get(port).getServer() == null) {
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), port + " is not opened");
        }
        if (!this.cache.get(port).getServer().stopClient(id)) {
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "server error");
        }
        return new SingleObjectResponse(HttpStatus.OK.value(), "successfully stopped");
    }
    
    public void push(Map<String, Object> data, int loggerId) {
        for (Entry<Integer, OutputInfoVO> entry: this.cache.entrySet()) {
            if (!entry.getValue().getProducerEps().containsKey(loggerId)) {
                EpsVO epsVO = new EpsVO();
                epsVO.setName(loggerMgr.getLogger(loggerId).getName());
                epsVO.setLastCheckTime(System.currentTimeMillis());
                entry.getValue().getProducerEps().put(loggerId, epsVO);
            }
            
            if (entry.getValue().getQueue().remainingCapacity() == 0) {
                entry.getValue().getQueue().poll();
                entry.getValue().getProducerEps().get(loggerId).addDeleted();
            }
            entry.getValue().getQueue().offer(data);
            entry.getValue().getProducerEps().get(loggerId).addCnt();
        }
    }

    public List<Map<String, Object>> poll(int port, int maxBuffer) {
        long time = System.currentTimeMillis();
        if (this.cache.get(port).getConsumerEps() == null) {
            EpsVO epsVO = new EpsVO();
            epsVO.setLastCheckTime(time);
            this.cache.get(port).setConsumerEps(epsVO);
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
        int cnt = this.cache.get(port).getQueue().drainTo(list, maxBuffer);
        this.cache.get(port).getConsumerEps().addCnt(cnt);
        return list;
    }

    public void removeProducerEps(int loggerId) {
        Set<Integer> set = new TreeSet<>(this.cache.keySet());
        for (int port: set) {
            if (this.cache.get(port).getProducerEps().containsKey(loggerId)) {
                this.cache.get(port).getProducerEps().remove(loggerId);
            }
        }
    }
    
    public List<Map<String, Object>> listProducerEpsHistory(int port){
        List<Map<String, Object>> res = new ArrayList<>();
        this.cache.get(port).getProducerEps().entrySet().forEach(e -> {
            Map<String, Object> logger = new HashMap<>();
            logger.put("name", loggerMgr.getLogger(e.getKey()).getName());
            List<Map<String, Long>> list = new ArrayList<>();
            e.getValue().getEpsHistory().stream().forEach(vo -> {
                Map<String, Long> tick = new HashMap<>();
                tick.put("x", vo.getTime());
                tick.put("y", (long)vo.getEps());
                list.add(tick);
            });
            logger.put("data", list);
            res.add(logger);
        });
        return res;
    }
    
    public LinkedBlockingQueue<EpsHistoryVO> listProducerEpsHistory(int port, int loggerId){
        return this.cache.get(port).getProducerEps().get(loggerId).getEpsHistory();
    }
}