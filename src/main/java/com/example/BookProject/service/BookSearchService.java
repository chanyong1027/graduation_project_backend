package com.example.BookProject.service;

import com.example.BookProject.domain.Library;
import com.example.BookProject.dto.LibraryBookDto;
import com.example.BookProject.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchService {

    private final KolisNetScraperService scraperService;
    private final LibraryRepository libraryRepository;

    public List<LibraryBookDto.SearchResponse> searchBookAndGetLibraries(String title, double userLat, double userLon, double distance) {
        // 1. KolisNet을 크롤링하여 책을 소장한 도서관 목록과 대출 가능 여부를 가져옵니다.
        log.info(">>>> KolisNet 크롤링 시작: '{}'", title);
        Map<String, Boolean> holdingLibrariesStatus = scraperService.getHoldingLibraries(title);
        log.info(">>>> 크롤링 결과: {}개 도서관이 '{}'을(를) 소장하고 있습니다.", holdingLibrariesStatus.size(), title);

        if (holdingLibrariesStatus.isEmpty()) {
            return new ArrayList<>(); // 소장 도서관이 없으면 빈 목록 반환
        }

        List<LibraryBookDto.SearchResponse> resultList = new ArrayList<>();

        // 2. 크롤링된 각 도서관에 대해, 우리 DB에서 상세 정보를 찾습니다.
        for (Map.Entry<String, Boolean> entry : holdingLibrariesStatus.entrySet()) {
            String scrapedLibName = entry.getKey();
            boolean isAvailable = entry.getValue();

            // TODO: 여기서도 정규화된 이름으로 DB를 검색하면 정확도를 높일 수 있습니다.
            log.info(">>>> DB에서 도서관 검색 중: '{}'", scrapedLibName);
            Optional<Library> dbLibraryOpt = libraryRepository.findByLibName(scrapedLibName);

            if (dbLibraryOpt.isPresent()) {
                Library dbLibrary = dbLibraryOpt.get();
                log.info("  -> [DB 매칭 성공] '{}'", scrapedLibName);

                // 3. 사용자가 설정한 '근처' 범위 안에 있는지 확인합니다.
                if (dbLibrary.getLatitude() != null && dbLibrary.getLongitude() != null &&
                        calculateDistance(userLat, userLon, dbLibrary.getLatitude(), dbLibrary.getLongitude()) <= distance) {

                    log.info("    -> [거리 통과] 사용자 근처에 있습니다. 최종 목록에 추가합니다.");
                    resultList.add(LibraryBookDto.SearchResponse.builder()
                            .library(dbLibrary)
                            .loanAvailable(isAvailable)
                            .build());
                } else {
                    log.warn("    -> [거리 미달] 사용자 위치에서 너무 멉니다.");
                }
            } else {
                log.warn("  -> [DB 매칭 실패] '{}' 이름을 가진 도서관을 DB에서 찾을 수 없습니다.", scrapedLibName);
            }
        }
        return resultList;
    }

    // LibraryService에 있던 거리 계산 메서드를 가져옵니다.
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}