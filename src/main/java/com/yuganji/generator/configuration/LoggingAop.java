package com.yuganji.generator.configuration;

import com.yuganji.generator.db.History;
import com.yuganji.generator.db.HistoryRepository;
import com.yuganji.generator.db.Logger;
import com.yuganji.generator.db.Output;
import com.yuganji.generator.model.LoggerDto;
import com.yuganji.generator.model.SingleObjectResponse;
import com.yuganji.generator.output.model.OutputDto;
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

    @Pointcut(value = "execution(public * com.yuganji.generator.controller.OutputRestController.*(..))")
    private void outputRest() { }

    @AfterReturning(value = "loggerRest() || outputRest()", returning = "returnValue")
    public void afterReturning(JoinPoint jp, Object returnValue) {
        if (jp.getArgs().length == 0 ||
                (!(jp.getArgs()[0] instanceof Logger) && !(jp.getArgs()[0] instanceof Output))) {
            return;
        }
        SingleObjectResponse res = (SingleObjectResponse) returnValue;
        History.HistoryBuilder history = History.builder();
        history = history.msg(res.getMsg());
        if (res.getStatus() != 200) {
            history = history.error(res.getMsg());
        }
        if (res.getData() instanceof Logger){
            Logger logger = (Logger) (res.getData());
            history = history.detail(logger.getYamlStr())
                    .fid(logger.getId())
                    .type("logger")
                    .ip(logger.getIp());
        } else if (res.getData() instanceof LoggerDto){
            LoggerDto logger = (LoggerDto) (res.getData());
            history = history.detail(logger.getYamlStr())
                    .fid(logger.getId())
                    .type("logger")
                    .ip(logger.getIp());
        } else if (res.getData() instanceof Output){
            Output output = (Output) (res.getData());
            history = history.detail(output.getInfo().toString())
                    .fid(output.getId())
                    .type("output")
                    .ip(output.getIp());
        }  else if (res.getData() instanceof OutputDto){
            OutputDto output = (OutputDto) (res.getData());
            history = history.detail(output.getInfo().toString())
                    .fid(output.getId())
                    .type("output")
                    .ip(output.getIp());
        }
        historyRepository.save(history.build());
    }


    @Around(value = "execution(public * com.yuganji.generator.output.OutputService.list(..))")
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
