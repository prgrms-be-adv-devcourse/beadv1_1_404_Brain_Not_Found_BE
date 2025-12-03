package com.ll.payment.deposit.model.vo.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record DepositHistoryPageResponse (
        String userCode,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious,
        List<DepositHistoryResponse> content
) {
    public static DepositHistoryPageResponse from(String userCode, Page<DepositHistoryResponse> page) {
        return new DepositHistoryPageResponse(
                userCode,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.getContent()
        );
    }
}
