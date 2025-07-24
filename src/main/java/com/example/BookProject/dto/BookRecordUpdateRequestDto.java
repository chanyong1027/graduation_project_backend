package com.example.BookProject.dto;

import com.example.BookProject.domain.ReadStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class BookRecordUpdateRequestDto {
    @NotNull(message = "변경할 독서 상태는 필수입니다.")
    private ReadStatus readStatus;
}