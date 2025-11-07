package com.ll.order.global.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {
    // 기본 페이지네이션
    public static Pageable getDefaultPageable(Pageable pageable) {
        if (pageable.getSort().isSorted()) // 정렬 조건이 있는 경우
            return pageable;
        else // 없으면 config 설정대로
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
    }
}
