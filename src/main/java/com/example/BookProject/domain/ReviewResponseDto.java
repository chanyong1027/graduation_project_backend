package com.example.BookProject.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {
    private final Long reviewId;
    private final String content;
    private final String reviewImg;
    private final Float rating;
    private final String authorNickname; // 작성자 닉네임
    private final Long bookId;
    private final LocalDateTime createdAt;

    public ReviewResponseDto(Review review) {
        this.reviewId = review.getId();
        this.content = review.getReviewContent();
        this.reviewImg = review.getReviewImg();
        this.rating = review.getRating();
        this.authorNickname = review.getUser().getUserNm(); // 예시: User 엔티티에 getNickname()이 있다고 가정
        this.bookId = review.getBook().getBookId();
        this.createdAt = review.getCreatedAt();
    }
}