package com.example.BookProject.repository;

import com.example.BookProject.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    // ISBN으로 책이 이미 DB에 저장되어 있는지 확인하기 위한 메소드
    Optional<Book> findByIsbn(String isbn);
}
