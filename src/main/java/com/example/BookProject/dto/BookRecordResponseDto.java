package com.example.BookProject.dto;

import com.example.BookProject.domain.BookRecord;
import com.example.BookProject.domain.ReadStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
public class BookRecordResponseDto {
    private final Long id;
    private final ReadStatus readStatus;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String review;
    private final Integer rating;
    private final BookDto.BookResponse book; // 책의 전체 정보를 담도록 변경

    public BookRecordResponseDto(BookRecord record) {
        this.id = record.getId();
        this.readStatus = record.getReadStatus();
        this.startDate = record.getStartDate();
        this.endDate = record.getEndDate();
        this.review = record.getReview();
        this.rating = record.getRating();
        this.book = new BookDto.BookResponse(record.getBook()); // BookResponse DTO 사용
    }
}
