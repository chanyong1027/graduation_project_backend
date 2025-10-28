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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/records")
public class BookRecordController {

    private final BookRecordService bookRecordService;



    // 1. 내 서재에 책 추가
    @PostMapping
    public ResponseEntity<BookRecordResponseDto> createRecord(@Valid @RequestBody BookRecordCreateRequestDto requestDto,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();// //Email로 가져옴
        BookRecordResponseDto responseDto = bookRecordService.createBookRecord(requestDto, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 2. 내 서재의 모든 책 조회 (상태별 필터링 가능)
    @GetMapping("/my")
    public ResponseEntity<List<BookRecordResponseDto>> getMyRecords(@RequestParam(value = "status", required = false) com.example.BookProject.domain.ReadStatus status,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        List<BookRecordResponseDto> records = bookRecordService.findMyBookRecords(userEmail, status);
        return ResponseEntity.ok(records);
    }

    // 3. 특정 기록 단건 조회
    @GetMapping("/{recordId}")
    public ResponseEntity<BookRecordResponseDto> getRecordById(@PathVariable("recordId") Long recordId,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        BookRecordResponseDto record = bookRecordService.findBookRecordById(recordId, userEmail);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/book/{isbn}")
    public ResponseEntity<BookRecordResponseDto> getMyRecordForBook(@PathVariable("isbn") String isbn,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        BookRecordResponseDto record = bookRecordService.findMyRecordForBook(userEmail, isbn);
        return ResponseEntity.ok(record);
    }

    // 4. 독서 상태 수정
    @PatchMapping("/{recordId}")
    public ResponseEntity<BookRecordResponseDto> updateRecordStatus(
            @PathVariable("recordId") Long recordId,
            @Valid @RequestBody BookRecordUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        BookRecordResponseDto updatedRecord = bookRecordService.updateBookRecordStatus(recordId, requestDto, userEmail);
        return ResponseEntity.ok(updatedRecord);
    }

    // 5. 후기 및 평점 수정
    @PutMapping("/{recordId}/review")
    public ResponseEntity<BookRecordResponseDto> updateReviewAndRating(
            @PathVariable("recordId") Long recordId,
            @RequestBody com.example.BookProject.dto.ReviewUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        BookRecordResponseDto updatedRecord = bookRecordService.updateReviewAndRating(recordId, requestDto, userEmail);
        return ResponseEntity.ok(updatedRecord);
    }

    // 6. 서재에서 책 삭제
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("recordId") Long recordId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        // 삭제하려는 기록이 현재 사용자의 것인지 확인하는 로직 추가 필요 (보안 강화)
        String userEmail = userDetails.getUsername();
        bookRecordService.deleteBookRecord(recordId, userEmail);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}