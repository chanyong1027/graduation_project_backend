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
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookService bookService; // BookService 주입
    private final BookRecordRepository bookRecordRepository;

    // 1. 생성 (Create)
    public BookRecordResponseDto createBookRecord(BookRecordCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        String isbn = requestDto.getIsbn(); // DTO에서 isbn을 받도록 변경 필요

        // DB에 책이 있는지 확인, 없으면 Aladin API를 통해 저장
        Book book = bookRepository.findByIsbn(isbn).orElseGet(() -> {
            bookService.saveBookByIsbn(isbn);
            return bookRepository.findByIsbn(isbn).orElseThrow(() -> new EntityNotFoundException("Book not found after saving"));
        });

        // 이미 등록된 책인지 확인
        bookRecordRepository.findByUserIdAndBook_Isbn(userId, book.getIsbn()).ifPresent(record -> {
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
    public BookRecordResponseDto findBookRecordById(Long recordId, Long userId) {
        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        if (!record.getUser().getId().equals(userId)) {
            throw new IllegalStateException("해당 독서 기록에 대한 조회 권한이 없습니다.");
        }
        return new BookRecordResponseDto(record);
    }

    @Transactional(readOnly = true)
    public List<BookRecordResponseDto> findMyBookRecords(Long userId, ReadStatus status) {
        List<BookRecord> records;
        if (status != null) {
            records = bookRecordRepository.findByUserIdAndReadStatus(userId, status);
        } else {
            records = bookRecordRepository.findByUserId(userId);
        }
        return records.stream()
                .map(BookRecordResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookRecordResponseDto findMyRecordForBook(Long userId, String isbn) {
        return bookRecordRepository.findByUserIdAndBook_Isbn(userId, isbn)
                .map(BookRecordResponseDto::new)
                .orElse(null);
    }

    // 3. 수정 (Update)
    public BookRecordResponseDto updateBookRecordStatus(Long recordId, BookRecordUpdateRequestDto requestDto, Long userId) {
        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        if (!record.getUser().getId().equals(userId)) {
            throw new IllegalStateException("해당 독서 기록에 대한 수정 권한이 없습니다.");
        }

        record.updateStatus(requestDto.getReadStatus());
        return new BookRecordResponseDto(record);
    }

    public BookRecordResponseDto updateReviewAndRating(Long recordId, com.example.BookProject.dto.ReviewUpdateRequestDto requestDto, Long userId) {
        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        if (!record.getUser().getId().equals(userId)) {
            throw new IllegalStateException("해당 독서 기록에 대한 수정 권한이 없습니다.");
        }

        record.updateReviewAndRating(requestDto.getReview(), requestDto.getRating());
        return new BookRecordResponseDto(record);
    }

    // 4. 삭제 (Delete)
    public void deleteBookRecord(Long recordId, Long userId) {
        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        if (!record.getUser().getId().equals(userId)) {
            throw new IllegalStateException("해당 독서 기록에 대한 삭제 권한이 없습니다.");
        }

        bookRecordRepository.deleteById(recordId);
    }
}
