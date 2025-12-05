package com.ll.products.domain.history.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userCode;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable =false)
    @CreatedDate
    private LocalDateTime createdAt;

    public ViewHistory(String userCode,String productCode){
        this.userCode=userCode;
        this.productCode=productCode;
    }

}
