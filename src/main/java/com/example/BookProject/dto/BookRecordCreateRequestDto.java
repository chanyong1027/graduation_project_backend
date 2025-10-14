package com.example.BookProject.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookRecordCreateRequestDto {

    @NotNull(message = "ISBN은 필수입니다.")
    private String isbn;
}
