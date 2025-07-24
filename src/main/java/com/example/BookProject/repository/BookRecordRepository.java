package com.example.BookProject.repository;

import com.example.BookProject.domain.BookRecord;
import com.example.BookProject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRecordRepository extends JpaRepository<BookRecord, Long> {
    List<BookRecord> findByUserId(Long userId);
    Optional<BookRecord> findByUserAndBookId(User user, Long BookId);
}
