package com.example.BookProject.service;

import com.example.BookProject.domain.*;
import com.example.BookProject.repository.BookRepository;
import com.example.BookProject.repository.ReviewRepository;
import com.example.BookProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Book book = bookRepository.findById(reviewRequestDto.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없습니다."));

        Review review = Review.builder()
                .reviewContent(reviewRequestDto.getContent())
                .reviewImg(reviewRequestDto.getReviewImg())
                .reviewRating(reviewRequestDto.getRating())
                .user(user)
                .book(book)
                .build();

        Review savedReview = reviewRepository.save(review);
        return new ReviewResponseDto(savedReview);
    }
}
