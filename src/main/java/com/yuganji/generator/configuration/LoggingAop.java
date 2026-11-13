package com.yuganji.generator.configuration;

import com.yuganji.generator.db.History;
import com.yuganji.generator.db.HistoryRepository;
import com.yuganji.generator.db.Logger;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.SingleObjectResponse;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class LoggingAop {

    @Autowired
    private HistoryRepository historyRepository;

    @Pointcut(value = "execution(public * com.yuganji.generator.controller.LoggerRestController.*(..))")
    private void loggerRest() { }

    @AfterReturning(value = "loggerRest()", returning = "returnValue")
    public void afterReturning(JoinPoint jp, Object returnValue) {
        if (jp.getArgs().length == 0 || !(jp.getArgs()[0] instanceof Logger)) {
            return;
        }
        SingleObjectResponse res = (SingleObjectResponse) returnValue;
        History.HistoryBuilder history = History.builder();
        history = history.msg(res.getMsg());
        if (res.getStatus() != 200) {
            history = history.error(res.getMsg());
        }
        if (res.getData() instanceof Logger) {
            Logger logger = (Logger) (res.getData());
            history = history.detail(logger.getYamlStr())
                    .fid(logger.getId())
                    .type("logger")
                    .ip(logger.getIp());
        } else if (res.getData() instanceof LoggerDto) {
            LoggerDto logger = (LoggerDto) (res.getData());
            history = history.detail(logger.getYamlStr())
                    .fid(logger.getId())
                    .type("logger")
                    .ip(logger.getIp());
        } else {
            return;
        }
        historyRepository.save(history.build());
    }
}
