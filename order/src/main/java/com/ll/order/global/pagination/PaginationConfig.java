package com.ll.order.global.pagination;

import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

public class PaginationConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolverList) {
        resolverList.add(mentoringClassPageableResolver());

        // TODO : 다른 도메인 resolver 생성
        //        resolverList.add(다른 도메인 resolver 메소드());
    }

    @Bean
    public PageableHandlerMethodArgumentResolver mentoringClassPageableResolver() {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

        resolver.setOneIndexedParameters(true); // 1부터 시작하는 페이지 번호 (false는 시작 페이지가 0)
        resolver.setMaxPageSize(50); // 최대 페이지 크기 50으로 제한
        resolver.setPageParameterName("page"); // 페이지 파라미터 이름
        resolver.setSizeParameterName("size"); // 크기 파라미터 이름

        resolver.setFallbackPageable(
                PageRequest.of(
                        0,
                        10, // 10개로 전체 제한
                        Sort.by(Sort.Direction.DESC, "createdAt") // 특별히 정렬 기준을 안보낼때 createdAt을 기준으로 정렬
                ));

        return resolver;
    }
}
