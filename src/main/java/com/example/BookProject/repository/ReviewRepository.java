package com.example.BookProject.repository;

import com.example.BookProject.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 책 ID로 모든 리뷰 조회
    List<Review> findByBookId(Long bookId);

    // 책 ISBN으로 모든 리뷰 조회
    @Query("SELECT r FROM Review r JOIN r.book b WHERE b.isbn = :isbn")
    List<Review> findByBookIsbn(@Param("isbn") String isbn);

    // 사용자 ID로 모든 리뷰 조회
    List<Review> findByUserId(Long userId);

    // 사용자 이메일로 모든 리뷰 조회
    @Query("SELECT r FROM Review r JOIN r.user u WHERE u.userEmail = :userEmail")
    List<Review> findByUserEmail(@Param("userEmail") String userEmail);

    // 특정 사용자의 특정 책에 대한 리뷰 조회
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.book.id = :bookId")
    Optional<Review> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    // 특정 사용자의 특정 ISBN 책에 대한 리뷰 조회
    @Query("SELECT r FROM Review r JOIN r.book b WHERE r.user.userEmail = :userEmail AND b.isbn = :isbn")
    Optional<Review> findByUserEmailAndBookIsbn(@Param("userEmail") String userEmail, @Param("isbn") String isbn);
}
