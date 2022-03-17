package com.yuganji.generator.history;

import com.yuganji.generator.db.History;
import com.yuganji.generator.db.HistoryRepository;
import com.yuganji.generator.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

    @Autowired
    private LoggerService loggerPropMng;

    @Autowired
    private HistoryRepository historyRepository;

    public Page<History> list(int page) {
        Page<History> list = historyRepository.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id")));
//        for (History history: list.getContent()) {
//            if (history.getType().equals("logger")) {
//                if (loggerPropMng.get(history.getFid()) != null) {
//                    history.setName(loggerPropMng.get(vo.getFid()).getName());
//                }
//            }
//        }
        return list;
    }

}
