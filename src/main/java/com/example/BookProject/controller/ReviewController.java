package com.example.BookProject.controller;

import com.example.BookProject.dto.ReviewRequestDto;
import com.example.BookProject.dto.ReviewResponseDto;
import com.example.BookProject.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody ReviewRequestDto requestDto) {
        // 실제로는 Spring Security 등을 통해 인증된 사용자의 ID를 가져와야 합니다.
        // 예시로 userId를 1L로 하드코딩합니다.
        Long currentUserId = 1L;

        ReviewResponseDto responseDto = reviewService.createReview(requestDto, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 여기에 리뷰 목록 조회, 단건 조회, 수정, 삭제 API가 추가될 수 있습니다.
}
