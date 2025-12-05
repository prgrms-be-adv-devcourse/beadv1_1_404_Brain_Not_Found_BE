package com.ll.products.domain.history.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.history.service.HistoryFacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/history")
public class HistoryController {

    HistoryFacadeService historyFacadeService;
    @GetMapping("recentview")
    public ResponseEntity<BaseResponse<List<String>>> getRecentView(
            @RequestHeader("X-User-Code") String userCode
    ){
        return BaseResponse.ok(historyFacadeService.getViewList(userCode));
    }

    @GetMapping("recentsearch")
    public ResponseEntity<BaseResponse<List<String>>> getRecentSearch(
            @RequestHeader("X-User-Code") String userCode
    ){
        return BaseResponse.ok(historyFacadeService.getSearchList(userCode));
    }
}
