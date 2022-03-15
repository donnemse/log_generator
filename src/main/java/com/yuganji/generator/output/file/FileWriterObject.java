package com.yuganji.generator.output.file;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.yuganji.generator.output.model.FileOutputConfig;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;


@Log4j2
public class FileWriterObject {
    private CsvWriter csvWriter;
    private FileWriter fileWriter;

    private FileOutputConfig config;
    private String prefix;

    private long lastTime;
    @Getter
    private long length;
    @Getter
    private long cnt;

    public FileWriterObject(FileOutputConfig config, String prefix) {
        this.config = config;
        this.prefix = prefix;
        File dir = new File(this.config.getPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void updateWriter(long time) {
        String date = new SimpleDateFormat("yyyyMMddHHmm").format(time);

        File f = this.getNextFile(
                new File(this.config.getPath(), this.prefix + "_" + date
                        + "." + this.config.getOutputType()));
        try {
            if (this.fileWriter != null) {
                this.fileWriter.flush();
                IOUtils.closeQuietly(this.fileWriter);
            }
            this.fileWriter = new FileWriter(f);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.debug("created file: " + f.getAbsolutePath());
        this.length = 0L;
        this.cnt = 0L;
        this.lastTime = time;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T getWriter() {
        if (csvWriter == null) {
            return (T) fileWriter;
        } else {
            return (T) csvWriter;
        }
    }

    public void write(String raw) throws IOException {
        long time = System.currentTimeMillis() / (60 * 1_000 * this.config.getFileRotationMin())
                * (60 * 1_000 * this.config.getFileRotationMin());
        if (time != this.lastTime) {
            this.updateWriter(time);
        }
        fileWriter.write(raw);
        fileWriter.write('\n');
        
        if (this.getCnt() % 100 == 0) {
            fileWriter.flush();
        }
        cnt++;
        length += raw.length() + 1;

        if (length > this.config.getMaxSize()) {
            this.updateWriter(System.currentTimeMillis());
        }
    }

    public void write(Map<String, Object> row) throws IOException {
        if (cnt == 0) {
            CsvWriterSettings settings = new CsvWriterSettings();
            settings.setHeaderWritingEnabled(true);
            settings.setHeaders(row.keySet().stream().toArray(String[]::new));
            this.csvWriter = new CsvWriter(settings);
            String h = this.csvWriter.writeHeadersToString();
            this.write(h);
        }
        this.write(csvWriter.writeRowToString(row));
    }

    private File getNextFile(File f) {
        if (!f.exists()) {
            return f;
        }
        String fname = FilenameUtils.getBaseName(f.getName());
        int idx = 1;
        while (f.exists()) {
            f = new File(f.getParentFile(),
                    fname + "_" + (idx++)
                            + "." + FilenameUtils.getExtension(f.getName()));
        }
        return f;
    }
}
