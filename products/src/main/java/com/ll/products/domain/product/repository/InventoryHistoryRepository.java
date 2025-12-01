package com.ll.products.domain.product.repository;

import com.ll.products.domain.product.model.entity.InventoryHistory;
import com.ll.products.domain.product.model.entity.InventoryHistoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    Optional<InventoryHistory> findByReferenceCode(String referenceCode);

}

