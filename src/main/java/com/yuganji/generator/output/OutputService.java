package com.yuganji.generator.output;

import com.yuganji.generator.db.Output;
import com.yuganji.generator.db.OutputRepository;
import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.EpsHistoryVO;
import com.yuganji.generator.model.EpsVO;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.model.OutputDto;
import com.yuganji.generator.output.model.SparrowOutput;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Log4j2
public class OutputService {
    
    @Getter
    private Map<Integer, OutputDto> cache;

    @Autowired
    private OutputRepository outputRepository;

    private LoggerService loggerMgr;

    @Autowired
    public OutputService (LoggerService loggerMgr) {
        this.cache = new ConcurrentHashMap<>();
        this.loggerMgr = loggerMgr;
    }

    @PostConstruct
    public void init(){
        log.debug(outputRepository.findAll());
        this.cache = outputRepository.findAll().stream().collect(Collectors.toMap(Output::getId, x -> x.toDto()));
        this.cache = new ConcurrentHashMap<>(this.cache);
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 20 * 1000)
    public void schedule() {
        this.cache.values().forEach(x -> {
            try {
                if (x.getStatus() == 1 && !x.getHandler().isRunning()) {
                    x.getHandler().startOutput();
            }
            } catch (OutputHandleException e) {
                x.setStatus(0);
                log.error(e.getMessage(), e);
            }

            this.cache.put(x.getId(), x);
        });
    }
    
    public OutputDto get(int id) {
        return this.cache.get(id);
    }

    public SingleObjectResponse add(Output output) {
        String msg = "Successfully saved" + output.getName();
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());

        try {
            output = outputRepository.save(output);
            this.cache.put(output.getId(), output.toDto());
            res.setMsg(msg);
            res.setData(output);
//            this.addHistory(output, msg, output.toString(), null);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            res.setMsg(e.getMessage());
//            this.addHistory(output, e.getMessage(), null, e.getMessage());
        }
        return res;
    }

    public SingleObjectResponse modify(Output output) {
        String msg = "output was modified. " + output.getName();
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value(), msg);
        if (!this.cache.containsKey(output.getId())) {
            res.setMsg("can not found output. " + output.getName());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return res;
        } else if (this.cache.get(output.getId()).getStatus() == 1) {
            res.setMsg(output.getName() + " is running now. stop it first.");
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return res;
        }
        try {
            this.cache.remove(output.getId());
            output = outputRepository.save(output);
//            outputMapper.updateOutput(output);
//            this.addHistory(output, msg, output.toString(), null);
            this.cache.put(output.getId(), output.toDto());
            res.setMsg(msg);
            res.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
//            this.addHistory(output, "could not modified utput. " + output.getName(), null, e.getMessage());
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public SingleObjectResponse remove(Output output) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        try {
            OutputDto info = this.cache.get(output.getId());
            if (info.getHandler().isRunning())  {
                throw new OutputHandleException("Stop it first");
            }
            String msg = "logger was removed. " + info.getName();
            res.setMsg(msg);

            outputRepository.deleteById(output.getId());
//            this.addHistory(output, msg, output.toString(), null);
            this.cache.remove(output.getId());
        } catch (Exception e) {
//            this.addHistory(output, "could not remove logger. " + output.getName(), null, e.getMessage());
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }
    
    public SingleObjectResponse start(Output output) {
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        OutputDto outputDto = this.cache.get(output.getId());
        String msg = "Successfully started " + outputDto.getName();
        outputDto.setIp(output.getIp());
        try {
            if (outputDto.getStatus() == 0 || !outputDto.getHandler().isRunning()) {
                if (!outputDto.getHandler().isReadyForRunning()) {
                    outputDto.resetHandler();
                }
                
                if (outputDto.getHandler().startOutput()) {
                    outputDto.setStatus(1);
                    res.setMsg(msg);
                    outputRepository.setStatus(output.getId(), 1, output.getIp());
                } else {
                    throw new OutputHandleException("Could not start output [" + outputDto.getName() + "]");
                }
            } else {
                throw new OutputHandleException("Already running output: " + outputDto.getName());
            }
            res.setData(outputDto);
        } catch (OutputHandleException e) {
            log.error(e.getMessage(), e);
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public SingleObjectResponse stop(Output output) {
        int id = output.getId();
        SingleObjectResponse res = new SingleObjectResponse(HttpStatus.OK.value());
        String msg = "Output was stopped: " + this.cache.get(id).getName();
        try {
            res.setData(this.cache.get(id));
            if (this.cache.containsKey(id) && this.cache.get(id).getStatus() == 1) {
                this.cache.get(id).getHandler().stopOutput();
                this.cache.get(id).setStatus(0);
                outputRepository.setStatus(id, 0, output.getIp());
                res.setMsg(msg);
                this.addHistory(this.cache.get(id), msg, null, null);
            } else {
                msg = "Output was not running status: " + this.cache.get(id).getName();
                res.setMsg(msg);
                res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res.setMsg(e.getMessage());
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return res;
    }

    public SingleObjectResponse closeClient(int id, String clientId, String ip) {
//        OutputVO vo = this.cache.get(id);
        try {
            if (((SparrowOutput) this.cache.get(id).getInfo()).closeClient(clientId)) {
                String message = "Successfully stopped client [" + clientId + "]";
//                histMapper.insertHistory(id, ip, TYPE, new Date().getTime(), message, null, null);
                return new SingleObjectResponse(HttpStatus.OK.value(), message);
            } else {
                throw new OutputHandleException("Could not stop client [" + clientId + "]");
            }
        } catch (OutputHandleException e) {
//            histMapper.insertHistory(id, ip, TYPE, new Date().getTime(), e.getMessage(), null, null);
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    public Collection<OutputDto> list() {
        return this.cache.values();
    }

    public void push(Map<String, Object> data, int loggerId) {
        
        this.cache.entrySet().parallelStream().forEach(entry -> {
            if (!entry.getValue().getProducerEps().containsKey(loggerId)) {
                EpsVO epsVO = new EpsVO();
                epsVO.setName(loggerMgr.get(loggerId).getName());
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
            this.cache.get(queueId).getProducerEps().remove(loggerId);
        }
    }
    
    public List<Map<String, Object>> listProducerEpsHistory(int port){
        List<Map<String, Object>> res = new ArrayList<>();
        this.cache.get(port).getProducerEps().forEach((key, value) -> {
            Map<String, Object> logger = new HashMap<>();
            logger.put("name", loggerMgr.get(key).getName());
            List<Map<String, Long>> list = new ArrayList<>();
            value.getEpsHistory().forEach(vo -> {
                Map<String, Long> tick = new HashMap<>();
                tick.put("x", vo.getTime());
                tick.put("y", (long) vo.getEps());
                list.add(tick);
            });
            logger.put("data", list);
            res.add(logger);
        });
        return res;
    }
    
    public Queue<EpsHistoryVO> listProducerEpsHistory(int port, int loggerId){
        return this.cache.get(port).getProducerEps().get(loggerId).getEpsHistory();
    }

    public boolean addHistory(OutputDto outputDto, String msg, String detail, String error) {
//        histMapper.insertHistory(output.getId(), output.getIp(), TYPE, new Date().getTime(), msg, detail, null);
        return true;
    }
}