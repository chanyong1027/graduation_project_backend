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
    private final Long bookId;
    private final String bookTitle;

    public BookRecordResponseDto(BookRecord record) {
        this.id = record.getId();
        this.readStatus = record.getReadStatus();
        this.startDate = record.getStartDate();
        this.endDate = record.getEndDate();
        this.bookId = record.getBook().getId();
        this.bookTitle = record.getBook().getTitle(); // Book 엔티티에 getTitle()이 있다고 가정
    }
}
