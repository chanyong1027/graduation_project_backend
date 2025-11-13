package com.example.BookProject.repository;

import com.example.BookProject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserEmail(String userEmail);
    Optional<User> findByUserNm(String userNm);

    boolean existsByUserEmail(String userEmail);
    boolean existsByUserNm(String userName);
}
