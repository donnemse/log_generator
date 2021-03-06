package com.yuganji.generator.output.file;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import com.yuganji.generator.configuration.ApplicationContextProvider;
import com.yuganji.generator.output.model.FileOutputConfig;
import com.yuganji.generator.queue.QueueService;

import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

@Log4j2
@EqualsAndHashCode(callSuper=false)
public class RawOutputWriter extends OutputFileWriter {

    public RawOutputWriter(int id, Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(
                new PropertyNamingStrategies.SnakeCaseStrategy());

        FileOutputConfig conf = mapper.convertValue(map, FileOutputConfig.class);
        super.setConfig(conf);
        super.outputId = id;
    }

    @Override
    public boolean startOutput() {
        return super.startOutput();
    }

    @Override
    public boolean stopOutput() {
        return super.stopOutput();
    }

    @Override
    public boolean isRunning() {
        return super.isAlive();
    }
    
    @Override
    public boolean isReadyForRunning() {
        return super.getState().equals(Thread.State.NEW);
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        boolean state = true;
        QueueService queueService = ApplicationContextProvider.getApplicationContext().getBean(QueueService.class);

        while (state) {
            try {
                List<Map<String, Object>> list = queueService.poll(this.getOutputId(), super.config.getBatchSize());
                if (list.size() == 0) {
                    Thread.sleep(1_000);
                    continue;
                }
                // TODO Avoid each event writes.
                for (Map<String, Object> row: list) {
                    String filenameBase = row.get(this.config.getFilePrefix()).toString();
                    switch (this.config.getOutputType()){
                        case "csv":
                            super.write(filenameBase, row);
                            break;
                        case "json":
                            super.write(filenameBase, gson.toJson(row));
                            break;
                        case "raw":
                            super.write(filenameBase, row.get("RAW").toString());
                            break;
                        default:
                    }
                }
                Thread.sleep(0, 10);
            }  catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                state = false;
                break;
            }
        }
    }
}
