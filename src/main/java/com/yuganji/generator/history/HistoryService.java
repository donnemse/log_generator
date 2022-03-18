package com.yuganji.generator.history;

import com.yuganji.generator.db.History;
import com.yuganji.generator.db.HistoryRepository;
import com.yuganji.generator.logger.LoggerService;
import com.yuganji.generator.output.OutputService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

    @Autowired
    private LoggerService loggerSerivce;
    
    @Autowired
    private OutputService outputService;

    @Autowired
    private HistoryRepository historyRepository;

    public Page<History> list(int page) {
        Page<History> list = historyRepository.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id")));
        for (History history: list.getContent()) {
            String name = "-";
            if (history.getType().equals("logger")) {
                if (loggerSerivce.get(history.getFid()) != null) {
                    name = loggerSerivce.get(history.getFid()).getName();
                }
            } else if (history.getType().equals("output")) {
                if (outputService.get(history.getFid()) != null) {
                    name = outputService.get(history.getFid()).getName();
                }
            }
            history.setName(name);
        }
        return list;
    }

}
