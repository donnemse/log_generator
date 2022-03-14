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
import java.util.stream.Collectors;

import com.yuganji.generator.output.model.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.logger.LoggerManager;
import com.yuganji.generator.model.EpsHistoryVO;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.model.SparrowOutput;
import com.yuganji.generator.mybatis.mapper.HistoryMapper;
import com.yuganji.generator.mybatis.mapper.OutputMapper;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.annotation.PostConstruct;

@Service
@Log4j2
public class OutputService {
    private static final String TYPE = "output";
    
    @Getter
    private Map<Integer, Output> cache;

    private OutputMapper outputMapper;
    
    private HistoryMapper histMapper;

    private LoggerManager loggerMgr;

    @Autowired
    public OutputService (
            LoggerManager loggerMgr, HistoryMapper histMapper,
            OutputMapper outputMapper) {
        this.cache = new ConcurrentHashMap<>();
        this.loggerMgr = loggerMgr;
        this.histMapper = histMapper;
        this.outputMapper = outputMapper;
    }

    @PostConstruct
    public void init(){
        this.cache = this.outputMapper.listOutput().stream().collect(
                Collectors.toMap(Output::getId, x -> x));
        this.cache = new ConcurrentHashMap<>(this.cache);
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 20 * 1000)
    public void schedule() {
        this.cache.values().stream().forEach(x -> {
            try {
                if (x.getStatus() == 1 && !x.getInfo().isRunning()) {
                    x.getInfo().startOutput();
            }
            } catch (OutputHandleException e) {
                x.setStatus(0);
                log.error(e.getMessage(), e);
            }

            this.cache.put(x.getId(), x);
        });
    }
    
    public Output get(int id) {
        return this.cache.get(id);
    }
    
    public SingleObjectResponse startOutput(Output vo) {
        
        String name = this.cache.get(vo.getId()).getName();
        try {
            if (vo.getInfo().startOutput()) {
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
    }

    public SingleObjectResponse stopOutput(int id, String ip) {
        Output vo = this.cache.get(id);
        try {
            if (vo.getInfo().stopOutput()) {
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
            if (((SparrowOutput) this.cache.get(id).getInfo()).closeClient(clientId)) {
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

    public Collection<Output> list() {
        return this.cache.values();
    }

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

    public List<Map<String, Object>> poll(int queueId, int maxBuffer) {
        long time = System.currentTimeMillis();
        if (this.cache.get(queueId).getConsumerEps() == null) {
            EpsVO epsVO = new EpsVO();
            epsVO.setLastCheckTime(time);
            this.cache.get(queueId).setConsumerEps(epsVO);
        }
        
        List<Map<String, Object>> list = new ArrayList<>();
        int cnt = this.cache.get(queueId).getQueue().drainTo(list, maxBuffer);
        this.cache.get(queueId).getConsumerEps().addCnt(cnt);
        return list;
    }

    public void removeProducerEps(int loggerId) {
        Set<Integer> set = new TreeSet<>(this.cache.keySet());
        for (int queueId: set) {
            if (this.cache.get(queueId).getProducerEps().containsKey(loggerId)) {
                this.cache.get(queueId).getProducerEps().remove(loggerId);
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