package com.yuganji.generator;

import com.yuganji.generator.db.History;
import com.yuganji.generator.db.HistoryRepository;
import com.yuganji.generator.db.Logger;
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

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Log4j2
public class LoggingAop {
    private List<String> loggerMethods = Arrays.asList(
            "add", "modify", "remove", "start", "stop"
    );

    @Autowired
    private HistoryRepository historyRepository;

    @Pointcut(value = "execution(public * com.yuganji.generator.logger.LoggerService.*(..))")
    private void loggerTarget() { }

    @AfterReturning(value = "loggerTarget()", returning = "returnValue")
    public void afterReturning(JoinPoint jp, Object returnValue) {
        if (!this.loggerMethods.contains(jp.getSignature().getName())){
            return;
        }
        Logger logger = null;
        String msg = null;
        String yaml = null;
        String error = null;
        SingleObjectResponse res = (SingleObjectResponse) returnValue;

        if (jp.getArgs()[0] instanceof Logger) {
            logger = ((Logger) jp.getArgs()[0]);
        }

        msg = res.getMsg();

        if (res.getStatus() == 200) {
            yaml = logger.getYamlStr();
        } else {
            error = res.getMsg();
        }

        historyRepository.save(
                History.builder()
                        .type("logger")
                        .ip(logger.getIp())
                        .fid(logger.getId())
                        .lastModified(System.currentTimeMillis())
                        .error(error)
                        .detail(yaml)
                        .msg(msg)
                        .build());
    }


    @Around(value = "loggerTarget()")
    public Object calPerformanceAdvice(ProceedingJoinPoint point) throws Throwable {
        StopWatch sw = new StopWatch();
        sw.start();
        Object result = point.proceed();
        sw.stop();

//        log.debug(point.getSignature().getName());
//        log.debug(point.getSignature().getDeclaringTypeName());
//        log.debug(point.getSignature());
//        log.debug(point.getTarget());
        log.debug("{} taken: {} ms",
                point.getSignature().toLongString(),  sw.getLastTaskTimeMillis());
        return result;
    }
}
