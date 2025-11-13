package com.example.deposit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class PaginationConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();

        // 페이지 번호 1부터 시작
        pageableResolver.setOneIndexedParameters(true);

        // page / size 파라미터 이름 지정
        pageableResolver.setPageParameterName("page");
        pageableResolver.setSizeParameterName("size");

        // 최대 페이지 크기 제한
        pageableResolver.setMaxPageSize(100);

        // 기본 정렬 기준 (요청에 sort 없을 경우)
        pageableResolver.setFallbackPageable(
                PageRequest.of(
                        0,                      // 기본 페이지 = 0
                        20,                     // 기본 size = 20
                        Sort.by(Sort.Direction.DESC, "createdAt") // 기본 정렬 필드
                )
        );

        resolvers.add(pageableResolver);
    }

}
