FROM azul/zulu-openjdk:8

COPY build/libs/LogGenerator-0.0.1-SNAPSHOT.jar /work/log-generator/
COPY src/main/resources/logback-spring.xml /work/log-generator/
COPY src/main/resources/application.properties /work/log-generator/
COPY config/IPCountry.csv /work/log-generator/config/
COPY db/logger.db /work/log-generator/db/

WORKDIR /work/log-generator

ENTRYPOINT ["java", "-jar", "LogGenerator-0.0.1-SNAPSHOT.jar", ">", "/dev/null", "2>&1"]


# docker build --tag log-generator:1.0 ./
# docker run -d -it -p 8080:8080 log-generator:1.0