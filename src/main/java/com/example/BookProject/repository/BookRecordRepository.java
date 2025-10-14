package com.example.BookProject.repository;

import com.example.BookProject.domain.BookRecord;
import com.example.BookProject.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRecordRepository extends JpaRepository<BookRecord, Long> {
    List<BookRecord> findByUserId(Long userId);
    List<BookRecord> findByUserIdAndReadStatus(Long userId, com.example.BookProject.domain.ReadStatus readStatus);
    Optional<BookRecord> findByUserIdAndBook_Isbn(Long userId, String isbn);
}
