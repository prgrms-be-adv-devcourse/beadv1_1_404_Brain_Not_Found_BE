package com.ll.products.domain.history.service;

import com.ll.products.domain.history.entity.SearchHistory;
import com.ll.products.domain.history.entity.ViewHistory;
import com.ll.products.global.util.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryFacadeService {

    private final RedisService redisService;
    private final HistoryService historyService;

    public void saveSearch(String userCode, String keyword) {
        try {
            redisService.pushSearchData(userCode, keyword);
        } catch (Exception e) {
            log.error("Redis Server Down in SaveSearch");
        } finally {
            historyService.saveSearchHistory(userCode, keyword);
        }
    }

    public void saveView(String userCode, String productCode) {
        try {
            redisService.pushViewData(userCode, productCode);
        } catch (Exception e) {
            log.error("Redis Server Down in SaveView");
        } finally {
            historyService.saveViewHistory(userCode, productCode);
        }
    }

    public List<String> getSearchList(String userCode) {
        try {
            List<String> keywords = redisService.getSearchData(userCode);
            if (keywords == null || keywords.isEmpty()) {
                keywords = historyService.getSearchHistory(userCode)
                        .stream()
                        .map(SearchHistory::getKeyWord)
                        .toList();

                keywords.forEach(keyword -> redisService.pushSearchData(userCode, keyword));
            }
            return keywords;
        } catch (Exception e) {
            log.error("Redis Server Down in GetSearchList");

            return historyService.getSearchHistory(userCode)
                    .stream()
                    .map(SearchHistory::getKeyWord)
                    .toList();
        }
    }

    public List<String> getViewList(String userCode) {

        try {
            List<String> products = redisService.getViewData(userCode);

            if (products == null || products.isEmpty()) {
                products = historyService.getViewHistory(userCode)
                        .stream()
                        .map(ViewHistory::getProductCode)
                        .toList();
                products.forEach(productCode -> redisService.pushViewData(userCode, productCode));
            }
            return products;

        } catch (Exception e) {
            log.error("Redis Server Down in GetViewList");

            return historyService.getViewHistory(userCode)
                    .stream()
                    .map(ViewHistory::getProductCode)
                    .toList();
        }
    }
}
