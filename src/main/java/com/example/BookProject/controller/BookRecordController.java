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

    // 현재 인증된 사용자 ID를 가져오는 헬퍼 메서드
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증된 사용자를 찾을 수 없습니다.");
        }
        // UserDetails 구현체에서 User 엔티티의 userId를 얻는 방법
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 실제 User 객체를 반환하는 CustomUserDetailsService를 구현했다면 캐스팅하여 userId를 얻을 수 있습니다.
        // 현재 User 엔티티가 UserDetails를 구현했으므로, 아래처럼 바로 캐스팅 가능.
        com.example.BookProject.domain.User currentUser = (com.example.BookProject.domain.User) authentication.getPrincipal();
        return currentUser.getId();
    }

    // 1. 내 서재에 책 추가
    @PostMapping
    public ResponseEntity<BookRecordResponseDto> createRecord(@Valid @RequestBody BookRecordCreateRequestDto requestDto) {
        Long currentUserId = getCurrentUserId(); // Spring Security 등으로 현재 사용자 ID를 가져와야 함
        BookRecordResponseDto responseDto = bookRecordService.createBookRecord(requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 2. 내 서재의 모든 책 조회
    @GetMapping("/my")
    public ResponseEntity<List<BookRecordResponseDto>> getMyRecords() {
        Long currentUserId = getCurrentUserId(); // Spring Security 등으로 현재 사용자 ID를 가져와야 함
        List<BookRecordResponseDto> records = bookRecordService.findMyBookRecords(currentUserId);
        return ResponseEntity.ok(records);
    }

    // 3. 특정 기록 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<BookRecordResponseDto> getRecordById(@PathVariable("id") Long recordId) {
        // 이 API는 recordId만으로 조회하므로, 특정 사용자 검증 로직이 추가될 수 있습니다.
        // 예를 들어, 해당 recordId가 현재 사용자의 것인지 확인하는 로직 등
        BookRecordResponseDto record = bookRecordService.findBookRecordById(recordId);
        return ResponseEntity.ok(record);
    }

    // 4. 독서 상태 수정
    @PatchMapping("/{id}")
    public ResponseEntity<BookRecordResponseDto> updateRecordStatus(
            @PathVariable("id") Long recordId,
            @Valid @RequestBody BookRecordUpdateRequestDto requestDto) {
        // 수정하려는 기록이 현재 사용자의 것인지 확인하는 로직 추가 필요 (보안 강화)
        // Long currentUserId = getCurrentUserId();
        // bookRecordService.checkOwnership(recordId, currentUserId); // 서비스에 소유권 확인 메서드 추가
        BookRecordResponseDto updatedRecord = bookRecordService.updateBookRecordStatus(recordId, requestDto);
        return ResponseEntity.ok(updatedRecord);
    }

    // 5. 서재에서 책 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("id") Long recordId) {
        // 삭제하려는 기록이 현재 사용자의 것인지 확인하는 로직 추가 필요 (보안 강화)
        // Long currentUserId = getCurrentUserId();
        // bookRecordService.checkOwnership(recordId, currentUserId);
        bookRecordService.deleteBookRecord(recordId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}