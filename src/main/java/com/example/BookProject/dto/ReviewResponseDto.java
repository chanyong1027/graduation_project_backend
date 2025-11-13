package com.example.BookProject.dto;

import com.example.BookProject.domain.Review;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {
    private final Long reviewId;
    private final String content;
    private final String reviewImg;
    private final Float rating;
    private final String authorNickname; // 작성자 닉네임
    private final Long authorId; // 작성자 ID
    private final Long bookId;
    private final String bookIsbn;
    private final String bookTitle;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ReviewResponseDto(Review review) {
        this.reviewId = review.getId();
        this.content = review.getReviewContent();
        this.reviewImg = review.getReviewImg();
        this.rating = review.getRating();
        this.authorNickname = review.getUser().getUserNm(); // User 엔티티의 userNm 사용
        this.authorId = review.getUser().getId();
        this.bookId = review.getBook().getId();
        this.bookIsbn = review.getBook().getIsbn();
        this.bookTitle = review.getBook().getTitle();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }
}