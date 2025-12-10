package com.ll.products.domain.history.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.history.service.HistoryFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "History", description = "상품 검색/조회 이력 데이터 관리")
@Slf4j
@RestController
@RequestMapping("api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryFacadeService historyFacadeService;

    @Operation(summary = "최근 상품 조회정보 조회" , description = "현재 로그인한 유저의 최근 상품 조회정보 30건을 조회합니다.")
    @GetMapping("recentview")
    public ResponseEntity<BaseResponse<List<String>>> getRecentView(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Code") String userCode
    ){
        return BaseResponse.ok(historyFacadeService.getViewList(userCode));
    }

    @Operation(summary = "최근 상품 검색정보 조회" , description = "현재 로그인한 유저의 최근 상품 검색정보 30건을 조회합니다.")
    @GetMapping("recentsearch")
    public ResponseEntity<BaseResponse<List<String>>> getRecentSearch(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Code") String userCode
    ){
        return BaseResponse.ok(historyFacadeService.getSearchList(userCode));
    }
}
