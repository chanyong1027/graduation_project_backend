package com.example.BookProject.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewUpdateRequestDto {
    private String review;
    private Integer rating;
}
