package com.example.BookProject.dto;

import com.example.BookProject.domain.ReadStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class BookRecordUpdateRequestDto {
    @NotNull(message = "변경할 독서 상태는 필수입니다.")
    private ReadStatus readStatus;

    // 선택적 필드: 제공되지 않으면 자동으로 오늘 날짜로 설정
    private LocalDate startDate;
    private LocalDate endDate;
}