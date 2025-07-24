package com.example.BookProject.controller;

import com.example.BookProject.dto.BookRecordCreateRequestDto;
import com.example.BookProject.dto.BookRecordResponseDto;
import com.example.BookProject.dto.BookRecordUpdateRequestDto;
import com.example.BookProject.service.BookRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class BookRecordController {

    private final BookRecordService bookRecordService;

    // 1. 내 서재에 책 추가
    @PostMapping
    public ResponseEntity<BookRecordResponseDto> createRecord(@Valid @RequestBody BookRecordCreateRequestDto requestDto) {
        Long currentUserId = 1L; // Spring Security 등으로 현재 사용자 ID를 가져와야 함
        BookRecordResponseDto responseDto = bookRecordService.createBookRecord(requestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 2. 내 서재의 모든 책 조회
    @GetMapping("/my")
    public ResponseEntity<List<BookRecordResponseDto>> getMyRecords() {
        Long currentUserId = 1L; // Spring Security 등으로 현재 사용자 ID를 가져와야 함
        List<BookRecordResponseDto> records = bookRecordService.findMyBookRecords(currentUserId);
        return ResponseEntity.ok(records);
    }

    // 3. 특정 기록 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<BookRecordResponseDto> getRecordById(@PathVariable("id") Long recordId) {
        BookRecordResponseDto record = bookRecordService.findBookRecordById(recordId);
        return ResponseEntity.ok(record);
    }

    // 4. 독서 상태 수정
    @PatchMapping("/{id}")
    public ResponseEntity<BookRecordResponseDto> updateRecordStatus(
            @PathVariable("id") Long recordId,
            @Valid @RequestBody BookRecordUpdateRequestDto requestDto) {
        BookRecordResponseDto updatedRecord = bookRecordService.updateBookRecordStatus(recordId, requestDto);
        return ResponseEntity.ok(updatedRecord);
    }

    // 5. 서재에서 책 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("id") Long recordId) {
        bookRecordService.deleteBookRecord(recordId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}