package com.example.BookProject.controller;

import com.example.BookProject.dto.ReviewRequestDto;
import com.example.BookProject.dto.ReviewResponseDto;
import com.example.BookProject.dto.ReviewUpdateRequestDto;
import com.example.BookProject.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 리뷰 관련 API 컨트롤러
 * - 책(ISBN) 기반으로 리뷰 작성/조회
 * - 사용자의 모든 리뷰 조회
 * - 리뷰 수정/삭제 (본인만 가능)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 생성 (ISBN 기반)
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        ReviewResponseDto responseDto = reviewService.createReview(requestDto, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 특정 책(ISBN)에 대한 모든 리뷰 조회
     * GET /api/reviews/books/{isbn}
     */
    @GetMapping("/books/{isbn}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByBookIsbn(@PathVariable String isbn) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByBookIsbn(isbn);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 현재 로그인한 사용자의 모든 리뷰 조회
     * GET /api/reviews/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        List<ReviewResponseDto> reviews = reviewService.getMyReviews(userEmail);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 특정 리뷰 단건 조회
     * GET /api/reviews/{reviewId}
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long reviewId) {
        ReviewResponseDto review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    /**
     * 리뷰 수정 (본인만 가능)
     * PATCH /api/reviews/{reviewId}
     */
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        ReviewResponseDto responseDto = reviewService.updateReview(reviewId, requestDto, userEmail);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 리뷰 삭제 (본인만 가능)
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        reviewService.deleteReview(reviewId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
