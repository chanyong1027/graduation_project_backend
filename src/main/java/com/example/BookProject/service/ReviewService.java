package com.example.BookProject.service;

import com.example.BookProject.domain.*;
import com.example.BookProject.dto.ReviewRequestDto;
import com.example.BookProject.dto.ReviewResponseDto;
import com.example.BookProject.dto.ReviewUpdateRequestDto;
import com.example.BookProject.repository.BookRepository;
import com.example.BookProject.repository.ReviewRepository;
import com.example.BookProject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * 리뷰 생성 (ISBN 기반)
     */
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto, String userEmail) {
        User user = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Book book = bookRepository.findByIsbn(reviewRequestDto.getIsbn())
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없습니다. ISBN: " + reviewRequestDto.getIsbn()));

        // 이미 해당 책에 대한 리뷰가 있는지 확인
        Optional<Review> existingReview = reviewRepository.findByUserEmailAndBookIsbn(userEmail, reviewRequestDto.getIsbn());
        if (existingReview.isPresent()) {
            throw new IllegalStateException("이미 해당 책에 대한 리뷰를 작성하셨습니다.");
        }

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

    /**
     * 특정 책(ISBN)에 대한 모든 리뷰 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByBookIsbn(String isbn) {
        List<Review> reviews = reviewRepository.findByBookIsbn(isbn);
        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 현재 로그인한 사용자의 모든 리뷰 조회
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getMyReviews(String userEmail) {
        List<Review> reviews = reviewRepository.findByUserEmail(userEmail);
        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 리뷰 ID로 단건 조회
     */
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 reviewId의 리뷰를 찾을 수 없습니다."));

        return new ReviewResponseDto(review);
    }

    /**
     * 리뷰 수정 (본인만 가능)
     */
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewUpdateRequestDto updateRequestDto, String userEmail) {
        User currentUser = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 reviewId의 리뷰를 찾을 수 없습니다: " + reviewId));

        // 권한 확인: 작성자 본인만 수정 가능
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("해당 리뷰를 수정할 권한이 없습니다.");
        }

        // DTO의 필드가 모두 optional이므로 null 체크를 하여 필요한 필드만 업데이트
        if (updateRequestDto.getContent() != null) {
            review.updateReviewContent(updateRequestDto.getContent());
        }
        if (updateRequestDto.getReviewImg() != null) {
            review.updateReviewImg(updateRequestDto.getReviewImg());
        }
        if (updateRequestDto.getRating() != null) {
            review.updateRating(updateRequestDto.getRating());
        }

        return new ReviewResponseDto(review);
    }

    /**
     * 리뷰 삭제 (본인만 가능)
     */
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        User currentUser = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 reviewId의 리뷰를 찾을 수 없습니다: " + reviewId));

        // 권한 확인: 작성자 본인만 삭제 가능
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("해당 리뷰를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }
}
