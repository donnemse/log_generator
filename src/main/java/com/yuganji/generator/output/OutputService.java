package com.yuganji.generator.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.yuganji.generator.logger.LoggerManager;
import com.yuganji.generator.model.EpsHistoryVO;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.model.OutputHandleException;
import com.yuganji.generator.model.OutputVO;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.model.SparrowOutput;
import com.yuganji.generator.mybatis.mapper.HistoryMapper;

import lombok.Getter;

@Service
public class OutputService {
    private static final String TYPE = "output";
    
    @Getter
    private Map<Integer, OutputVO> cache;

    private HistoryMapper histMapper;

    private LoggerManager loggerMgr;

    @Autowired
    public OutputService (
            LoggerManager loggerMgr, HistoryMapper histMapper) {
        this.cache = new ConcurrentHashMap<>();
        this.loggerMgr = loggerMgr;
        this.histMapper = histMapper;
    }
    
    public OutputVO get(int id) {
        return this.cache.get(id);
    }
    
    public SingleObjectResponse startOutput(OutputVO vo) {
        
        String name = this.cache.get(vo.getId()).getName();
        try {
            if (vo.getHandler().startOutput()) {
                String message = "Successfully started " + name;
              histMapper.insertHistory(vo.getId(), vo.getIp(), TYPE, new Date().getTime(), message, null, null);
              return new SingleObjectResponse(HttpStatus.OK.value(), message);
            } else {
                throw new OutputHandleException("Could not start output [" + name + "]");
            }
        } catch (OutputHandleException e) {
            histMapper.insertHistory(vo.getId(), vo.getIp(), TYPE, new Date().getTime(), e.getMessage(), null, null);
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
        
//        if (cache.containsKey(vo.getPort())) {
//            String message = "Already opened " + vo.getPort() + " port.";
//            histMapper.insertHistory(vo.getPort(), vo.getIp(), TYPE, new Date().getTime(), message, null, null);
//            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
//        } else {
//            vo.setServer(new TCPSocketServer(vo.getPort(), this));
//            vo.getServer().startServer();
//            this.cache.put(vo.getPort(), vo);
//            String message = "Successfully opened " + vo.getPort() + " port.";
//            histMapper.insertHistory(vo.getPort(), vo.getIp(), TYPE, new Date().getTime(), message, null, null);
//            return new SingleObjectResponse(HttpStatus.OK.value(), message);
//        }
    }

    public SingleObjectResponse stopOutput(int id, String ip) {
        OutputVO vo = this.cache.get(id);
        try {
            if (vo.getHandler().stopOutput()) {
                String message = "Successfully stopped " + vo.getName();
                histMapper.insertHistory(vo.getId(), vo.getIp(), TYPE, new Date().getTime(), message, null, null);
                return new SingleObjectResponse(HttpStatus.OK.value(), message);
            } else {
                throw new OutputHandleException("Could not stop output [" + vo.getName() + "]");
            }
        } catch (OutputHandleException e) {
            histMapper.insertHistory(vo.getId(), vo.getIp(), TYPE, new Date().getTime(), e.getMessage(), null, null);
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    public SingleObjectResponse closeClient(int id, String clientId, String ip) {
//        OutputVO vo = this.cache.get(id);
        try {
            if (((SparrowOutput) this.cache.get(id).getHandler()).closeClient(clientId)) {
                String message = "Successfully stopped client [" + clientId + "]";
                histMapper.insertHistory(id, ip, TYPE, new Date().getTime(), message, null, null);
                return new SingleObjectResponse(HttpStatus.OK.value(), message);
            } else {
                throw new OutputHandleException("Could not stop client [" + clientId + "]");
            }
        } catch (OutputHandleException e) {
            histMapper.insertHistory(id, ip, TYPE, new Date().getTime(), e.getMessage(), null, null);
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    public Collection<OutputVO> list() {
        return this.cache.values();
    }
    
//    public SingleObjectResponse stopClient(int port, String id) {
//        if (!this.cache.containsKey(port) ||
//                this.cache.get(port).getServer() == null) {
//            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), port + " is not opened");
//        }
//        if (!this.cache.get(port).getServer().stopClient(id)) {
//            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "server error");
//        }
//        return new SingleObjectResponse(HttpStatus.OK.value(), "successfully stopped");
//    }
    
    public void push(Map<String, Object> data, int loggerId) {
        
        this.cache.entrySet().parallelStream().forEach(entry -> {
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
        });
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