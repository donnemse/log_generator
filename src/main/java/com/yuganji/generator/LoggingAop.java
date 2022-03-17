package com.yuganji.generator;

import com.yuganji.generator.db.History;
import com.yuganji.generator.db.HistoryRepository;
import com.yuganji.generator.db.Logger;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.SingleObjectResponse;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

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
        Logger logger = ((Logger) jp.getArgs()[0]);
        String msg = res.getMsg();
        String yaml = null;
        String error = null;

        if (res.getStatus() != 200) {
            error = res.getMsg();
        }
        if (res.getData() instanceof Logger){
            yaml = ((Logger) res.getData()).getYamlStr();
        } else if (res.getData() instanceof LoggerDto){
            yaml = ((LoggerDto) res.getData()).getYamlStr();
        }

        historyRepository.save(
                History.builder()
                        .type("logger").ip(logger.getIp()).fid(logger.getId())
                        .lastModified(System.currentTimeMillis()).error(error)
                        .detail(yaml).msg(msg).build());
    }


    @Around(value = "loggerRest()")
    public Object calPerformanceAdvice(ProceedingJoinPoint point) throws Throwable {
        StopWatch sw = new StopWatch();
        sw.start();
        Object result = point.proceed();
        sw.stop();
        log.debug("{} taken: {} ms",
                point.getSignature().toLongString(),  sw.getLastTaskTimeMillis());
        return result;
    }
}
