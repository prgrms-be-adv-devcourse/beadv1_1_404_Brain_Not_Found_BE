package com.ll.products.domain.history.repository;

import com.ll.products.domain.history.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory,Long> {
    List<SearchHistory> findTop30ByUserCodeOrderByCreatedAtDesc(String userCode);
}
