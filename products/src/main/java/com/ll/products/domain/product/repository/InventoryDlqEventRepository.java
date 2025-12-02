package com.ll.products.domain.product.repository;

import com.ll.products.domain.product.model.entity.InventoryDlqEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryDlqEventRepository extends JpaRepository<InventoryDlqEvent, Long> {

}

