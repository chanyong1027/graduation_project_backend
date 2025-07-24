package com.example.BookProject.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", updatable = false)
    private Long id;

    @Column(name = "content", nullable = false, length = 30000)
    private String reviewContent;

    @Column(name = "review_img", length = 1000)
    private String reviewImg;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @CreationTimestamp // 엔티티가 처음 생성될 때 시간 자동 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티가 수정될 때마다 시간 자동 저장
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Review(String reviewContent, String reviewImg, Float reviewRating, Book book, User user) {
        this.reviewContent = reviewContent;
        this.reviewImg = reviewImg;
        this.rating = reviewRating;
        this.book = book;
        this.user = user;
    }
}
