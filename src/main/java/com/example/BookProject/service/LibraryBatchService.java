// com/example/BookProject/service/LibraryBatchService.java

package com.example.BookProject.service;

import com.example.BookProject.domain.Library;
import com.example.BookProject.dto.LibraryDto;
import com.example.BookProject.repository.LibraryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryBatchService {

    private final LibraryRepository libraryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // ObjectMapper는 여러 번 생성할 필요 없이 재사용 가능하므로 필드로 선언합니다.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${external.api.data4library}")
    private String data4LibApiKey;

    @Transactional
    public void fetchAndSaveAllLibrariesFromData4Lib() {
        log.info("정보나루 API를 이용한 도서관 데이터 배치 작업을 시작합니다. (ObjectMapper 방식)");

        Set<Long> existingLibCodes = libraryRepository.findAll().stream()
                .map(Library::getD4lLibCode)
                .collect(Collectors.toSet());
        log.info("DB에서 {}개의 기존 도서관 코드를 메모리에 로드했습니다.", existingLibCodes.size());

        int pageNo = 1;
        final int pageSize = 1000;

        while (true) {
            String url = String.format(
                    "http://data4library.kr/api/libSrch?authKey=%s&pageNo=%d&pageSize=%d&format=json",
                    data4LibApiKey, pageNo, pageSize
            );

            try {
                // 1. API로부터 응답을 순수 String 형태로 받습니다.
                String jsonResponse = restTemplate.getForObject(url, String.class);

                // 2. 받아온 String을 ObjectMapper를 사용해 DTO 객체로 변환합니다.
                LibraryDto.Data4LibResponse responseDto = objectMapper.readValue(jsonResponse, LibraryDto.Data4LibResponse.class);

                // --- 이하 로직은 기존과 동일 --- //

                if (responseDto == null || responseDto.getResponse() == null || responseDto.getResponse().getLibs() == null || responseDto.getResponse().getLibs().isEmpty()) {
                    log.info("페이지 {}에서 더 이상 가져올 도서관 데이터가 없어 작업을 종료합니다.", pageNo);
                    break;
                }

                List<LibraryDto.Data4LibResponse.LibData> libDataList = responseDto.getResponse().getLibs().stream()
                        .map(LibraryDto.Data4LibResponse.Lib::getLib)
                        .collect(Collectors.toList());

                List<Library> librariesToSave = new ArrayList<>();

                for (LibraryDto.Data4LibResponse.LibData libData : libDataList) {
                    if (existingLibCodes.contains(libData.getLibCode())) {
                        continue;
                    }
                    if (libData.getLatitude() == null || libData.getLongitude() == null || libData.getLatitude().isEmpty() || libData.getLongitude().isEmpty()) {
                        log.warn("좌표 정보가 없는 도서관 데이터는 건너뜁니다: libCode={}, libName={}", libData.getLibCode(), libData.getLibName());
                        continue;
                    }

                    try {
                        Library newLibrary = Library.builder()
                                .d4lLibCode(libData.getLibCode())
                                .libName(libData.getLibName())
                                .address(libData.getAddress())
                                .tel(libData.getTel())
                                .homepage(libData.getHomepage())
                                .latitude(Double.parseDouble(libData.getLatitude()))
                                .longitude(Double.parseDouble(libData.getLongitude()))
                                .build();
                        librariesToSave.add(newLibrary);
                    } catch (NumberFormatException e) {
                        log.error("좌표 정보 파싱 실패: libCode={}, libName={}, lat={}, lon={}",
                                libData.getLibCode(), libData.getLibName(), libData.getLatitude(), libData.getLongitude());
                    }
                }

                if (!librariesToSave.isEmpty()) {
                    libraryRepository.saveAll(librariesToSave);
                    log.info("페이지 {}에서 {}개의 신규 도서관을 DB에 저장했습니다.", pageNo, librariesToSave.size());
                }

                if (libDataList.size() < pageSize) {
                    log.info("마지막 페이지입니다. 총 {} 페이지를 처리했습니다.", pageNo);
                    break;
                }

            } catch (JsonProcessingException e) {
                log.error("JSON 파싱 중 에러가 발생했습니다. 페이지: {}", pageNo, e);
                break; // 파싱 실패 시 루프 중단
            } catch (Exception e) {
                log.error("API 호출 중 에러가 발생했습니다. 페이지: {}", pageNo, e);
                break; // 기타 에러 발생 시 루프 중단
            }

            pageNo++;
        }
        log.info("도서관 데이터 배치 작업을 완료했습니다.");
    }
}