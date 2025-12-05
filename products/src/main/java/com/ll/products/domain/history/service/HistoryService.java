package com.ll.products.domain.history.service;

import com.ll.products.domain.history.entity.SearchHistory;
import com.ll.products.domain.history.entity.ViewHistory;
import com.ll.products.domain.history.repository.SearchHistoryRepository;
import com.ll.products.domain.history.repository.ViewHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final ViewHistoryRepository viewHistoryRepository;

    public void saveSearchHistory(String userCode, String keyword) {
        searchHistoryRepository.save(new SearchHistory(userCode, keyword));
    }

    public void saveViewHistory(String userCode, String productCode) {
        viewHistoryRepository.save(new ViewHistory(userCode, productCode));
    }

    public List<SearchHistory> getSearchHistory(String userCode) {
        return searchHistoryRepository.findTop30ByUserCodeOrderByCreatedAtDesc(userCode);
    }

    public List<ViewHistory> getViewHistory(String userCode) {
        return viewHistoryRepository.findTop30ByUserCodeOrderByCreatedAtDesc(userCode);
    }
}
