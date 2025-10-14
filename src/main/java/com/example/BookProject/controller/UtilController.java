package com.example.BookProject.controller;

import com.example.BookProject.service.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utils")
@RequiredArgsConstructor
public class UtilController {

    private final UtilService utilService;

    @GetMapping("/coord-to-region")
    public ResponseEntity<String> getRegionFromCoords(@RequestParam("x") double longitude, @RequestParam("y") double latitude) {
        String result = utilService.getRegionFromCoords(longitude, latitude);
        return ResponseEntity.ok(result);
    }
}
