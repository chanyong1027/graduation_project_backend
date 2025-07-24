package com.example.BookProject.service;

import com.example.BookProject.domain.Book;
import com.example.BookProject.domain.BookRecord;
import com.example.BookProject.domain.ReadStatus;
import com.example.BookProject.domain.User;
import com.example.BookProject.dto.BookRecordCreateRequestDto;
import com.example.BookProject.dto.BookRecordResponseDto;
import com.example.BookProject.dto.BookRecordUpdateRequestDto;
import com.example.BookProject.repository.BookRecordRepository;
import com.example.BookProject.repository.BookRepository;
import com.example.BookProject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookRecordService {
    private final BookRecordRepository bookRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // 1. 생성 (Create)
    public BookRecordResponseDto createBookRecord(BookRecordCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Book book = bookRepository.findById(requestDto.getBookId()).orElseThrow(() -> new EntityNotFoundException("Book not found"));

        // 이미 등록된 책인지 확인
        bookRecordRepository.findByUserAndBookId(user, requestDto.getBookId()).ifPresent(record -> {
            throw new IllegalStateException("이미 서재에 등록된 책입니다.");
        });

        BookRecord newRecord = BookRecord.builder()
                .user(user)
                .book(book)
                .readStatus(ReadStatus.Wish) // 초기 상태는 '읽을 책'
                .build();

        BookRecord savedRecord = bookRecordRepository.save(newRecord);
        return new BookRecordResponseDto(savedRecord);
    }

    // 2. 조회 (Read)
    @Transactional(readOnly = true)
    public BookRecordResponseDto findBookRecordById(Long recordId) {
        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));
        return new BookRecordResponseDto(record);
    }

    @Transactional(readOnly = true)
    public List<BookRecordResponseDto> findMyBookRecords(Long userId) {
        return bookRecordRepository.findByUserId(userId).stream()
                .map(BookRecordResponseDto::new)
                .collect(Collectors.toList());
    }

    // 3. 수정 (Update)
    public BookRecordResponseDto updateBookRecordStatus(Long recordId, BookRecordUpdateRequestDto requestDto) {
        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        // 엔티티 내부의 비즈니스 메서드를 사용하여 상태 업데이트
        record.updateStatus(requestDto.getReadStatus());

        // dirty checking에 의해 트랜잭션 종료 시 자동으로 update 쿼리 실행됨
        return new BookRecordResponseDto(record);
    }

    // 4. 삭제 (Delete)
    public void deleteBookRecord(Long recordId) {
        if (!bookRecordRepository.existsById(recordId)) {
            throw new EntityNotFoundException("Book record not found");
        }
        bookRecordRepository.deleteById(recordId);
    }
}
