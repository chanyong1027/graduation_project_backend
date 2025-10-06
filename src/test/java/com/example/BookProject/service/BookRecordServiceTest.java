package com.example.BookProject.service;

import com.example.BookProject.domain.Book;
import com.example.BookProject.domain.BookRecord;
import com.example.BookProject.domain.User;
import com.example.BookProject.dto.BookRecordCreateRequestDto;
import com.example.BookProject.repository.BookRecordRepository;
import com.example.BookProject.repository.BookRepository;
import com.example.BookProject.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// ✅ 1. Mockito 확장 기능을 사용한다고 선언합니다. @SpringBootTest 대신 사용합니다.
@ExtendWith(MockitoExtension.class)
class BookRecordServiceTest {

    // ✅ 2. @InjectMocks: 테스트할 실제 객체입니다. 내부에 @Mock으로 선언된 가짜 객체들이 주입됩니다.
    @InjectMocks
    private BookRecordService bookRecordService;

    // ✅ 3. @Mock: 가짜로 만들 객체들입니다. DB와 상호작용하지 않고 우리가 시키는 대로만 동작합니다.
    @Mock
    private BookRecordRepository bookRecordRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;

    @DisplayName("이미 등록된 책을 추가하면 예외가 발생한다")
    @Test
    void createBookRecord_fail_alreadyExists() {
        // GIVEN (준비) ----------------------------------------------------
        // 테스트에 필요한 요청 DTO와 더미(dummy) 객체들을 준비합니다.
        Long userId = 1L;
        Long bookId = 100L;
        BookRecordCreateRequestDto requestDto = new BookRecordCreateRequestDto(bookId);

        User fakeUser = new User("test@test.com", "1234", "테스터");
        Book fakeBook = Book.builder().isbn("1234567890").title("테스트 책").build();
        BookRecord fakeRecord = BookRecord.builder().build();

        // ✅ 4. 가짜 Repository들이 어떻게 행동할지 '시나리오'를 정의합니다.
        // "userRepository.findById(userId)가 호출되면, fakeUser를 담은 Optional을 반환해줘"
        when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
        // "bookRepository.findById(bookId)가 호출되면, fakeBook을 담은 Optional을 반환해줘"
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(fakeBook));
        // "bookRecordRepository.findByUserAndBookId가 호출되면, 이미 존재하는 것처럼 BookRecord Optional을 반환해줘"
        when(bookRecordRepository.findByUserAndBookId(any(User.class), any(Long.class)))
                .thenReturn(Optional.of(fakeRecord)); // 비어있지 않은 Optional을 반환 -> "이미 존재함"을 흉내


        // WHEN & THEN (실행 및 검증) --------------------------------------
        // bookRecordService.createBookRecord를 실행했을 때,
        assertThatThrownBy(() -> bookRecordService.createBookRecord(requestDto, userId))
                .isInstanceOf(IllegalStateException.class) // IllegalStateException 예외가 터지고,
                .hasMessage("이미 서재에 등록된 책입니다.");     // 메시지가 정확한지 검증합니다.
    }
}