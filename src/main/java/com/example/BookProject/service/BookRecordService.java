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

    // 헬퍼 메서드: userEmail로 User 객체 찾기 (반복 사용)
    private User findUserByEmail(String userEmail) {
        return userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userEmail));
    }

    // 1. 생성 (Create)
    public BookRecordResponseDto createBookRecord(BookRecordCreateRequestDto requestDto, String userEmail) {
        User user = findUserByEmail(userEmail);
        String isbn = requestDto.getIsbn();

        // DB에 책이 있는지 확인, 없으면 Aladin API를 통해 저장
        Book book = bookRepository.findByIsbn(isbn).orElseGet(() -> {
            bookService.saveBookByIsbn(isbn);
            return bookRepository.findByIsbn(isbn).orElseThrow(() -> new EntityNotFoundException("Book not found after saving"));
        });

        /// 이미 등록된 책인지 확인 (user.getId() 사용)
        bookRecordRepository.findByUserIdAndBook_Isbn(user.getId(), book.getIsbn()).ifPresent(record -> {
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
    public BookRecordResponseDto findBookRecordById(Long recordId, String userEmail) {
        // 1. userEmail로 User 조회
        User user = findUserByEmail(userEmail);

        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        // 2. 권한 확인 (user.getId() 사용)
        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 독서 기록에 대한 조회 권한이 없습니다.");
        }
        return new BookRecordResponseDto(record);
    }

    @Transactional(readOnly = true)
    public List<BookRecordResponseDto> findMyBookRecords(String userEmail, ReadStatus status) {
        // 4. userEmail로 User 조회
        User user = findUserByEmail(userEmail);

        List<BookRecord> records;
        if (status != null) {
            // 5. user.getId() 사용
            records = bookRecordRepository.findByUserIdAndReadStatus(user.getId(), status);
        } else {
            // 5. user.getId() 사용
            records = bookRecordRepository.findByUserId(user.getId());
        }
        return records.stream()
                .map(BookRecordResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookRecordResponseDto findMyRecordForBook(String userEmail, String isbn) {
        // 7. userEmail로 User 조회
        User user = findUserByEmail(userEmail);

        // 8. user.getId() 사용
        return bookRecordRepository.findByUserIdAndBook_Isbn(user.getId(), isbn)
                .map(BookRecordResponseDto::new)
                .orElse(null); // (조회 결과가 없으면 null 반환 - 필요시 예외처리)
    }

    // 3. 수정 (Update)
    public BookRecordResponseDto updateBookRecordStatus(Long recordId, BookRecordUpdateRequestDto requestDto, String userEmail) {
        // 1. userEmail로 User 조회
        User user = findUserByEmail(userEmail);

        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        // 2. 권한 확인 (user.getId() 사용)
        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 독서 기록에 대한 수정 권한이 없습니다.");
        }

        record.updateStatus(requestDto.getReadStatus());
        return new BookRecordResponseDto(record);
    }

    public BookRecordResponseDto updateReviewAndRating(Long recordId, com.example.BookProject.dto.BookRecordReviewUpdateRequestDto requestDto, String userEmail) {
        // 1. userEmail로 User 조회
        User user = findUserByEmail(userEmail);

        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        // 2. 권한 확인 (user.getId() 사용)
        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 독서 기록에 대한 수정 권한이 없습니다.");
        }

        record.updateReviewAndRating(requestDto.getReview(), requestDto.getRating());
        return new BookRecordResponseDto(record);
    }

    // 4. 삭제 (Delete)
    public void deleteBookRecord(Long recordId, String userEmail) {
        // 1. userEmail로 User 조회
        User user = findUserByEmail(userEmail);

        BookRecord record = bookRecordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Book record not found"));

        // 2. 권한 확인 (user.getId() 사용)
        if (!record.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 독서 기록에 대한 삭제 권한이 없습니다.");
        }

        bookRecordRepository.deleteById(recordId);
    }
}
