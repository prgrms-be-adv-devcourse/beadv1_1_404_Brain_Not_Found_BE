package com.ll.auth.repository;

import com.ll.auth.model.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth,Long> {

    Optional<Auth> findByUserCode(String userCode);

    void deleteByUserCode(String userCode);
}
