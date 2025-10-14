package com.example.BookProject.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedBookSearchResponseDto {
    private final int totalResults;
    private final List<BookDto.BookSearchResponse> books;

    public PaginatedBookSearchResponseDto(int totalResults, List<BookDto.BookSearchResponse> books) {
        this.totalResults = totalResults;
        this.books = books;
    }
}
