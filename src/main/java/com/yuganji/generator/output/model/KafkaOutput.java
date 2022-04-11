package com.yuganji.generator.output.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yuganji.generator.exception.OutputHandleException;
import com.yuganji.generator.model.AbstractOutputHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class KafkaOutput extends AbstractOutputHandler {
    @JsonIgnore
    private transient int id;

    @JsonIgnore
    private OutputKafkaProducer producer;
    public KafkaOutput(int id, Map<String, Object> info) {
        this.id = id;
        this.producer = new OutputKafkaProducer(id, info);
    }

    @Override
    public boolean startOutput() throws OutputHandleException {
        return this.producer.startOutput();
    }

    @Override
    public boolean stopOutput() throws OutputHandleException {
        return this.producer.stopOutput();
    }

    @Override
    public boolean isRunning() throws OutputHandleException {
        return this.producer.isRunning();
    }

    @Override
    public boolean isReadyForRunning() {
        return this.producer.isReadyForRunning();
    }

}
