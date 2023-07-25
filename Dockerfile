FROM azul/zulu-openjdk:8

COPY build/libs/LogGenerator-0.0.1-SNAPSHOT.jar /work/log_generator/
COPY src/main/resources/logback-spring.xml /work/log_generator/
COPY src/main/resources/application.properties /work/log_generator/
COPY config/IPCountry.csv /work/log_generator/config/
COPY db/logger.db /work/log_generator/db/

WORKDIR /work/log_generator

ENTRYPOINT ["java", "-jar", "LogGenerator-0.0.1-SNAPSHOT.jar", ">", "/dev/null", "2>&1"]


# docker build --tag log_generator:1.0 ./
# docker run -d -it -p 8080:8080 log_generator:1.0
