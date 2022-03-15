package com.yuganji.generator.output.file;

import com.yuganji.generator.ApplicationContextProvider;
import com.yuganji.generator.output.OutputService;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Deprecated
@Builder
@Data
@Log4j2
@EqualsAndHashCode(callSuper=false)
public class CsvOutputWriter extends OutputFileWriter {
    private String type;
    private String path;
    private int fileRotationMin;
    private String fileFormat;
    private String fileName;
    private int maxSize;
    private int batchSize;
    private int outputId;

    @Builder.Default
    private transient OutputService outputService =
            ApplicationContextProvider.getApplicationContext().getBean(OutputService.class);

    @Override
    public boolean startOutput() {
        this.outputId = outputId;
        super.startOutput();
        return false;
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
    public void run() {
        File dir = new File(this.path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        while (this.state) {
            this.fileRotationMin = 10;
            long time = System.currentTimeMillis();
            time = time / (60 * 1_000 * this.fileRotationMin) * (60 * 1_000 * this.fileRotationMin);
            String date = new SimpleDateFormat("yyyyMMddHHmm").format(time);

            try {
                List<Map<String, Object>> list = outputService.poll(this.getOutputId(), batchSize);
                if (list.size() == 0) {
                    Thread.sleep(1_000);
                    continue;
                }
                for (Map<String, Object> row: list) {
                    File file = new File(this.getPath(), row.get(this.fileName).toString() + "_" + date + "." + type);
                    super.write(row.get(this.fileName).toString(), row);
                }
                
                Thread.sleep(0, 10);
            }  catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }





    }
}
