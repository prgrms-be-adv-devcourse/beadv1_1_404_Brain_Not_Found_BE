package com.ll.products.domain.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.domain.search.document.ProductDocument;
import com.ll.products.domain.search.dto.ProductSearchResponse;
import com.ll.products.domain.search.exception.EsIndexException;
import com.ll.products.domain.search.exception.EsSearchException;
import com.ll.products.domain.search.repository.ProductSearchRepository;
import com.ll.products.global.util.ProductAuthValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    @Value("${cloud.aws.s3.base-url}")
    private String s3BaseUrl;

    public Page<ProductSearchResponse> search(
            String keyword,
            Long categoryId,
            Integer minPrice,
            Integer maxPrice,
            String status,
            Pageable pageable
    ) {
        log.debug("상품 검색 요청: keyword={}, categoryId={}, price={}-{}, status={}, pageable={}",
                keyword, categoryId, minPrice, maxPrice, status, pageable);
        try {
            pageable = applyDefaultSort(keyword, pageable);
            Query query = buildDynamicQuery(keyword, categoryId, minPrice, maxPrice, status);
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withPageable(pageable)
                    .build();
            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
            List<ProductDocument> products = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .toList();
            Page<ProductDocument> documents = new PageImpl<>(
                    products,
                    pageable,
                    searchHits.getTotalHits()
            );
            log.debug("검색 결과: totalElements={}, totalPages={}, currentPage={}, size={}",
                    documents.getTotalElements(), documents.getTotalPages(), documents.getNumber(), documents.getSize());
            return documents.map(d -> ProductSearchResponse.from(d, s3BaseUrl));
        } catch (Exception e) {
            throw new EsSearchException("ES 검색 중 오류가 발생했습니다.");
        }
    }




    // 기본 정렬 전략 설정
    private static Pageable applyDefaultSort(String keyword, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            if (keyword == null || keyword.isBlank()) {
                pageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
                log.debug("최신순 정렬");
            } else {
                log.debug("관련도순 졍렬");
            }
        } else {
            log.debug("사용자 지정 정렬 사용: {}", pageable.getSort());
        }
        return pageable;
    }

    // 동적 쿼리 생성
    private Query buildDynamicQuery(String keyword, Long categoryId, Integer minPrice, Integer maxPrice, String status) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 키워드 검색
        if (keyword != null && !keyword.isBlank()) {
            List<Query> queries = new ArrayList<>();

            // multi_match: 기본 토큰 매칭(name->3.0, categoryName->1.5, description->1.0)
            queries.add(Query.of(q -> q
                    .multiMatch(m -> m
                            .query(keyword)
                            .fields("name^3.0", "categoryName^1.5", "description^1.0")
                            .type(TextQueryType.BestFields)
                    )
            ));

            // match_phrase(name): 순서 일치 시 추가 점수 2.0
            queries.add(Query.of(q -> q
                    .matchPhrase(mp -> mp
                            .field("name")
                            .query(keyword)
                            .boost(2.0f)
                    )
            ));

            // match_phrase(categoryName): 순서 일치 시 추가 점수 1.5
            queries.add(Query.of(q -> q
                    .matchPhrase(mp -> mp
                            .field("categoryName")
                            .query(keyword)
                            .boost(1.5f)
                    )
            ));

            // match_phrase(description): 순서 일치 시 추가 점수 1.0
            queries.add(Query.of(q -> q
                    .matchPhrase(mp -> mp
                            .field("description")
                            .query(keyword)
                            .boost(1.0f)
                    )
            ));

            boolBuilder.should(queries);
            boolBuilder.minimumShouldMatch("1");
        }

        List<Query> filterQueries = new ArrayList<>();

        // 카테고리 필터링
        if (categoryId != null) {
            filterQueries.add(Query.of(q -> q
                    .term(t -> t
                            .field("categoryId")
                            .value(categoryId)
                    )
            ));
        }

        // 상태 필터링
        if (status != null && !status.isBlank()) {
            filterQueries.add(Query.of(q -> q
                    .term(t -> t
                            .field("status")
                            .value(status)
                    )
            ));
        }

        // 가격 범위 필터링
        if (minPrice != null && maxPrice != null) {
            filterQueries.add(Query.of(q -> q
                    .range(r -> r
                            .number(n -> n
                                    .field("price")
                                    .gte((double) minPrice)
                                    .lte((double) maxPrice)
                            )
                    )
            ));
        } else if (minPrice != null) {
            filterQueries.add(Query.of(q -> q
                    .range(r -> r
                            .number(n -> n
                                    .field("price")
                                    .gte((double) minPrice)
                            )
                    )
            ));
        } else if (maxPrice != null) {
            filterQueries.add(Query.of(q -> q
                    .range(r -> r
                            .number(n -> n
                                    .field("price")
                                    .lte((double) maxPrice)
                            )
                    )
            ));
        }
        boolBuilder.filter(filterQueries);

        // 키워드가 없으면 전체 검색
        if (keyword == null || keyword.isBlank()) {
            boolBuilder.must(Query.of(q -> q.matchAll(m -> m)));
        }
        return Query.of(q -> q.bool(boolBuilder.build()));
    }

    public void reindexAll(String role) {
        ProductAuthValidator.validateAdmin(role);
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);

            // 기존 인덱스 삭제
            if (indexOps.exists()) {
                indexOps.delete();
                log.info("기존 Elasticsearch 인덱스 삭제 완료");
            }

            // 새로운 인덱스 생성
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping());
            log.info("새로운 Elasticsearch 인덱스 생성 완료");

            // 모든 상품 조회
            List<Product> products = productRepository.findAllByIsDeletedFalse();
            log.info("MySQL에서 조회된 상품 수: {}", products.size());

            // ProductDocument로 변환
            List<ProductDocument> documents = products.stream()
                    .map(ProductDocument::from)
                    .toList();

            // es에 저장
            productSearchRepository.saveAll(documents);
            log.info("Elasticsearch 재색인 완료: {} 건", documents.size());
        }catch (Exception e){
            log.error("Elasticsearch 재색인 실패: {}", e.getMessage(), e);
            throw new EsIndexException("es 재색인인 중 오류가 발생했습니다.");
        }
    }
}