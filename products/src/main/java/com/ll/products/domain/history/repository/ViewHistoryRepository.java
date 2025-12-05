package com.ll.products.domain.history.repository;

import com.ll.products.domain.history.entity.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory,Long> {
    List<ViewHistory> findTop30ByUserCodeOrderByCreatedAtDesc(String userCode);
}
