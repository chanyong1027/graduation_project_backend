package com.example.BookProject.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookRecordCreateRequestDto {

    @NotNull(message = "도서 ID는 필수입니다.")
    private Long bookId;
}
