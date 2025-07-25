package com.example.BookProject.repository;

import com.example.BookProject.domain.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    // 정보나루 API의 libCode로 DB에 저장된 도서관이 있는지 확인
    Optional<Library> findByLibCode(Long libCode);
}
