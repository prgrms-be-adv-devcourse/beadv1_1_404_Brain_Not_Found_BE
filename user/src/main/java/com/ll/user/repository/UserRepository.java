package com.ll.user.repository;

import com.ll.user.model.entity.User;
import com.ll.user.model.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialIdAndSocialProvider(String socialId, SocialProvider provider);
    Optional<User> findByCode(String code);
}
