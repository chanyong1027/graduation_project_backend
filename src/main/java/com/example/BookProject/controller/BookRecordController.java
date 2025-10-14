package com.example.BookProject.controller;

import com.example.BookProject.dto.BookRecordCreateRequestDto;
import com.example.BookProject.dto.BookRecordResponseDto;
import com.example.BookProject.dto.BookRecordUpdateRequestDto;
import com.example.BookProject.service.BookRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class BookRecordController {

    private final BookRecordService bookRecordService;
    private final com.example.BookProject.repository.UserRepository userRepository; // UserRepository 주입

    // 현재 인증된 사용자 ID를 가져오는 헬퍼 메서드
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증된 사용자를 찾을 수 없습니다.");
        }
        String userEmail = authentication.getName();
        com.example.BookProject.domain.User currentUser = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다: " + userEmail));
        return currentUser.getId();
    }

    // 1. 내 서재에 책 추가
    @PostMapping
    public ResponseEntity<BookRecordResponseDto> createRecord(@Valid @RequestBody BookRecordCreateRequestDto requestDto) {
        Long currentUserId = getCurrentUserId(); // Spring Security 등으로 현재 사용자 ID를 가져와야 함
        BookRecordResponseDto responseDto = bookRecordService.createBookRecord(requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 2. 내 서재의 모든 책 조회 (상태별 필터링 가능)
    @GetMapping("/my")
    public ResponseEntity<List<BookRecordResponseDto>> getMyRecords(@RequestParam(value = "status", required = false) com.example.BookProject.domain.ReadStatus status) {
        Long currentUserId = getCurrentUserId();
        List<BookRecordResponseDto> records = bookRecordService.findMyBookRecords(currentUserId, status);
        return ResponseEntity.ok(records);
    }

    // 3. 특정 기록 단건 조회
    @GetMapping("/{recordId}")
    public ResponseEntity<BookRecordResponseDto> getRecordById(@PathVariable("recordId") Long recordId) {
        Long currentUserId = getCurrentUserId();
        BookRecordResponseDto record = bookRecordService.findBookRecordById(recordId, currentUserId);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/book/{isbn}")
    public ResponseEntity<BookRecordResponseDto> getMyRecordForBook(@PathVariable("isbn") String isbn) {
        Long currentUserId = getCurrentUserId();
        BookRecordResponseDto record = bookRecordService.findMyRecordForBook(currentUserId, isbn);
        return ResponseEntity.ok(record);
    }

    // 4. 독서 상태 수정
    @PatchMapping("/{recordId}")
    public ResponseEntity<BookRecordResponseDto> updateRecordStatus(
            @PathVariable("recordId") Long recordId,
            @Valid @RequestBody BookRecordUpdateRequestDto requestDto) {
        Long currentUserId = getCurrentUserId();
        BookRecordResponseDto updatedRecord = bookRecordService.updateBookRecordStatus(recordId, requestDto, currentUserId);
        return ResponseEntity.ok(updatedRecord);
    }

    // 5. 후기 및 평점 수정
    @PutMapping("/{recordId}/review")
    public ResponseEntity<BookRecordResponseDto> updateReviewAndRating(
            @PathVariable("recordId") Long recordId,
            @RequestBody com.example.BookProject.dto.ReviewUpdateRequestDto requestDto) {
        Long currentUserId = getCurrentUserId();
        BookRecordResponseDto updatedRecord = bookRecordService.updateReviewAndRating(recordId, requestDto, currentUserId);
        return ResponseEntity.ok(updatedRecord);
    }

    // 6. 서재에서 책 삭제
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("recordId") Long recordId) {
        // 삭제하려는 기록이 현재 사용자의 것인지 확인하는 로직 추가 필요 (보안 강화)
        Long currentUserId = getCurrentUserId();
        bookRecordService.deleteBookRecord(recordId, currentUserId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}