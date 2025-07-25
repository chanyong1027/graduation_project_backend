package com.example.BookProject.repository;

import com.example.BookProject.domain.User;
import com.example.BookProject.domain.UserLibrary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLibraryRepository extends JpaRepository<UserLibrary, Long> {

    // 특정 유저가 등록한 모든 도서관 정보 조회
    List<UserLibrary> findByUser(User user);

    // 특정 유저가 해당 도서관을 이미 추가했는지 확인
    boolean existsByUserAndLibrary_Id(User user, Long libraryId);

    // 즐겨찾기 삭제를 위해 정보 조회
    Optional<UserLibrary> findByUserAndLibrary_Id(User user, Long libraryId);
}
