package com.example.BookProject.dto;

import com.example.BookProject.domain.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookDto {

    @Getter
    @NoArgsConstructor
    public static class BookResponse {
        private Long bookId;
        private String title;
        private String author;
        private String publisher;
        private String isbn;
        private String bookImg;
        private String description;

        public BookResponse(Book book) {
            this.bookId = book.getId();
            this.title = book.getTitle();
            this.author = book.getAuthor();
            this.publisher = book.getPublisher();
            this.isbn = book.getIsbn();
            this.bookImg = book.getBookImg();
            this.description = book.getDescription();
        }
    }
}
