package com.example.BookProject.controller;

import com.example.BookProject.domain.BookDto;
import com.example.BookProject.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping("/search")
    public ResponseEntity<Void> searchAndSaveBooks(@RequestParam String query){
        bookService.searchAndSaveBooks(query);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<BookDto.BookResponse>> getAllBooks(){

        List<BookDto.BookResponse> bookResponses = bookService.findAllBooks();

        return ResponseEntity.ok().body(bookResponses);
    }
}
