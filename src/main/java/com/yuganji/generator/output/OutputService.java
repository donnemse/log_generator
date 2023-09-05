package com.yuganji.generator.output;

import com.yuganji.generator.db.Output;
import com.yuganji.generator.db.OutputRepository;
import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.model.EpsHistoryVO;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.model.OutputDto;
import com.yuganji.generator.output.model.SparrowOutput;
import com.yuganji.generator.queue.QueueObject;
import com.yuganji.generator.queue.QueueService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class OutputService {
    
    @Getter
    private Map<Integer, OutputDto> cache;

    @Autowired
    private OutputRepository outputRepository;

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private QueueService queueSerivce;

    @PostConstruct
    public void init(){
        this.cache = outputRepository.findAll().stream().collect(Collectors.toMap(Output::getId, Output::toDto));
        this.cache.forEach((k, v) -> {

            this.queueSerivce.putIfAbsent(
                    k, new QueueObject(v.getMaxQueueSize(),
                            v.getInfo().get("filter") != null? v.getInfo().get("filter").toString(): null));
        });
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 20 * 1000)
    public void schedule() {
        this.cache.values().forEach(x -> {
            try {
                if (x.getStatus() == 1 && !x.getHandler().isRunning()) {
                    if (!x.getHandler().isReadyForRunning()) {
                        x.resetHandler();
                    }
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
            this.queueSerivce.putIfAbsent(
                    output.getId(),
                    new QueueObject(output.getMaxQueueSize(),
                            output.getInfo().get("filter") != null? output.getInfo().get("filter").toString(): null));
            res.setMsg(msg);
            res.setData(output);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            res.setMsg(e.getMessage());
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
            this.cache.put(output.getId(), output.toDto());
            this.queueSerivce.putIfAbsent(
                    output.getId(),
                    new QueueObject(output.getMaxQueueSize(),
                            output.getInfo().get("filter") != null? output.getInfo().get("filter").toString(): null));
            res.setMsg(msg);
            res.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
            this.cache.remove(output.getId());
            this.queueSerivce.removeQueueObj(output.getId());
        } catch (Exception e) {
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
                return new SingleObjectResponse(HttpStatus.OK.value(), message);
            } else {
                throw new OutputHandleException("Could not stop client [" + clientId + "]");
            }
        } catch (OutputHandleException e) {
            return new SingleObjectResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }

    public Collection<OutputDto> list() {
        return this.cache.values();
    }

    public List<Map<String, Object>> listProducerEpsHistory(int outputId){
        List<Map<String, Object>> res = new ArrayList<>();
        
        queueSerivce.getQueueObj(outputId).getProducerEps().forEach((key, value) -> {
            Map<String, Object> logger = new HashMap<>();
            logger.put("name", loggerService.get(key).getName());
            List<Map<String, Long>> list = new ArrayList<>();
            value.getEpsHistory().forEach(vo -> {
                Map<String, Long> tick = new HashMap<>();
                tick.put("x", vo.getTime());
                tick.put("y", Math.round(vo.getEps()));
                list.add(tick);
            });
            logger.put("data", list);
            res.add(logger);
        });
        return res;
    }
    
    public Queue<EpsHistoryVO> listProducerEpsHistory(int outputId, int loggerId){
        return queueSerivce.getQueueObj(outputId).getProducerEps().get(loggerId).getEpsHistory();
    }
}