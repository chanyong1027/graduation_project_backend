package com.example.BookProject.controller;

import com.example.BookProject.dto.ReviewRequestDto;
import com.example.BookProject.dto.ReviewResponseDto;
import com.example.BookProject.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 현재 인증된 사용자 ID를 가져오는 헬퍼 메서드 (재사용)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증된 사용자를 찾을 수 없습니다.");
        }
        com.example.BookProject.domain.User currentUser = (com.example.BookProject.domain.User) authentication.getPrincipal();
        return currentUser.getId();
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody ReviewRequestDto requestDto) {
        Long currentUserId = getCurrentUserId();
        ReviewResponseDto responseDto = reviewService.createReview(requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/books/{isbn}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByBookIsbn(@PathVariable String isbn) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByBookIsbn(isbn);
        return ResponseEntity.ok(reviews);
    }

    // 여기에 리뷰 목록 조회, 단건 조회, 수정, 삭제 API가 추가될 수 있습니다.
    @GetMapping("/books/{bookId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReivewsByBookId(@PathVariable Long bookId) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long reviewId) {
        ReviewResponseDto review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(@PathVariable Long reviewId, @Valid @RequestBody ReviewRequestDto requestDto) {
        Long currentUserId = getCurrentUserId();
        ReviewResponseDto responseDto = reviewService.updateReview(reviewId, requestDto, currentUserId);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        Long currentUserId = getCurrentUserId();
        reviewService.deleteReview(reviewId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
