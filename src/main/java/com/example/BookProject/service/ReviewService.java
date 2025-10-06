package com.example.BookProject.service;

import com.example.BookProject.domain.*;
import com.example.BookProject.dto.ReviewRequestDto;
import com.example.BookProject.dto.ReviewResponseDto;
import com.example.BookProject.repository.BookRepository;
import com.example.BookProject.repository.ReviewRepository;
import com.example.BookProject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public List<ReviewResponseDto> getReviewsByBookId(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("해당 bookId의 책을 찾을 수 없습니다."));

        return reviewRepository.findById(book.getId()).stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponseDto getReviewById(Long reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 reviewId의 리뷰를 찾을 수 없습니다."));

        return new ReviewResponseDto(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByBookIsbn(String isbn) {
        List<Review> reviews = reviewRepository.findByBookIsbn(isbn);
        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("해당 reviewId의 리뷰를 찾을 수 없습니다." + reviewId));

        if(!review.getUser().getId().equals(userId)){
            throw new IllegalStateException("해당 review를 수정할 권한이 없습니다.");
        }

        // DTO의 필드가 모두 optional이므로 null 체크를 하여 필요한 필드만 업데이트
        if (reviewRequestDto.getContent() != null) {
            review.updateReviewContent(reviewRequestDto.getContent());
        }
        if (reviewRequestDto.getReviewImg() != null) {
            review.updateReviewImg(reviewRequestDto.getReviewImg());
        }
        if (reviewRequestDto.getRating() != null) {
            review.updateRating(reviewRequestDto.getRating());
        }

        return new ReviewResponseDto(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 reviewId의 리뷰를 찾을 수 없습니다." + reviewId));

        // 작성자 본인만 삭제 가능하도록 검증
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("해당 review를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }
}
