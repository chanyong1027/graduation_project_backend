// com/example/BookProject/service/LibraryBatchService.java

package com.example.BookProject.service;

import com.example.BookProject.domain.Library;
import com.example.BookProject.dto.LibraryDto;
import com.example.BookProject.dto.NlssLibraryDto;
import com.example.BookProject.repository.LibraryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j // 로그를 찍기 위해 추가
@Service
@RequiredArgsConstructor
public class LibraryBatchService {

    private final LibraryRepository libraryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.api.data4library}")
    private String data4LibApiKey;

    /**
     * 국가도서관 통계시스템 API를 호출하여 모든 도서관 정보를 DB에 저장합니다.
     * 이미 저장된 도서관은 건너뜁니다.
     */
    @Transactional
    public void fetchAndSaveLibraries() {
        log.info("도서관 데이터 배치 작업을 시작합니다.");

        Set<String> existingLibCodes = libraryRepository.findAll().stream()
                .map(Library::getNlssLibCode)
                .collect(Collectors.toSet());
        log.info("DB에서 {}개의 기존 도서관 코드를 메모리에 로드했습니다.", existingLibCodes.size());

        int page = 1;
        int size = 100; // API가 허용하는 최대 사이즈로 설정하여 호출 횟수를 줄입니다.

        while (true) {
            String urlTemplate = "https://libsta.go.kr/nlstatapi/api/v1/libinfo?page=%d&size=%d";
            String requestUrl = String.format(urlTemplate, page, size);

            // 1. API 호출
            NlssLibraryDto responseDto = restTemplate.getForObject(requestUrl, NlssLibraryDto.class);

            if (responseDto == null || responseDto.getResult() == null || responseDto.getResult().getList() == null || responseDto.getResult().getList().isEmpty()) {
                log.info("더 이상 가져올 도서관 데이터가 없습니다. 페이지: {}", page);
                break;
            }

            List<NlssLibraryDto.Lib> libs = responseDto.getResult().getList();
            List<Library> librariesToSave = new ArrayList<>();
            //log.info("페이지 {}에서 {}개의 도서관 데이터를 가져왔습니다.", page, libs.size());

            for (NlssLibraryDto.Lib libDto : libs) {
                // 2. DB에 이미 존재하는지 확인 (nlssLibCode 기준)
                if (libDto.getLatitude() == null || libDto.getLongitude() == null) {
                    log.warn("좌표 정보가 없는 도서관 데이터는 건너뜁니다: {}", libDto.getLibName());
                    continue; // 현재 반복을 중단하고 다음 도서관으로 넘어감
                }
                // [성능 개선!] DB에 매번 물어보지 않고, 메모리에 있는 Set에서 바로 확인합니다. (훨씬 빠름)
                if (!existingLibCodes.contains(libDto.getLibCode())) {
                    Library newLibrary = Library.builder()
                            .nlssLibCode(libDto.getLibCode())
                            .libName(libDto.getLibName())
                            .address(libDto.getAddress())
                            .homepage(libDto.getHomepage())
                            .longitude(libDto.getLongitude())
                            .latitude(libDto.getLatitude())
                            .tel(libDto.getTel())
                            .build();
                    librariesToSave.add(newLibrary);
                }


                // 2만개의 insert쿼리 날려서 국가도서관통계시스템에서 받은 2만개의 도서관이 있는지확인하는 쿼리
//                libraryRepository.findByNlssLibCode(libDto.getLibCode()).ifPresentOrElse(
//                        // 이미 존재하면, 로그만 남기고 아무것도 하지 않음 (향후 업데이트 로직 추가 가능)
//                        library -> log.trace("이미 존재하는 도서관입니다: {}", library.getLibName()),
//                        // 존재하지 않으면,
//                        () -> {
//                            // 3. DTO -> Entity로 변환하여 저장
//                            Library newLibrary = Library.builder()
//                                    .nlssLibCode(libDto.getLibCode())
//                                    .libName(libDto.getLibName())
//                                    .address(libDto.getAddress())
//                                    .homepage(libDto.getHomepage())
//                                    .longitude(libDto.getLongitude())
//                                    .latitude(libDto.getLatitude())
//                                    .tel(libDto.getTel())
//                                    .build();
//                            libraryRepository.save(newLibrary);
//                            log.debug("신규 도서관을 저장했습니다: {}", newLibrary.getLibName());
//                        }
//                );
//            }
//            // 만약 가져온 도서관 수가 size보다 작으면, 그게 마지막 페이지라는 의미이므로 종료
//            if (libs.size() < size) {
//                log.info("마지막 페이지입니다. 총 {} 페이지를 처리했습니다.", page);
//                break;
            }
            if (!librariesToSave.isEmpty()) {
                libraryRepository.saveAll(librariesToSave);
                log.info("페이지 {}에서 {}개의 신규 도서관을 저장했습니다.", page, librariesToSave.size());
            }

            if (libs.size() < size) {
                log.info("마지막 페이지입니다. 총 {} 페이지를 처리했습니다.", page);
                break;
            }
            page++; // 다음 페이지로
        }
        log.info("도서관 데이터 배치 작업을 완료했습니다.");
    }

    @Transactional
    public void reconcileWithData4Lib() {
        log.info("Step 2: 도서관 정보나루 데이터 연결 작업을 시작합니다.");

        // 2. 정보나루 API를 호출하여 참여 도서관 목록을 가져옵니다.
        //    (주의: 이 API는 전체 목록을 한번에 주므로 페이지네이션이 필요 없습니다.)
        String url = "http://data4library.kr/api/libSrch?authKey=" + data4LibApiKey + "&pageSize=100&format=json";
        //LibraryDto.Data4LibResponse response = restTemplate.getForObject(url, LibraryDto.Data4LibResponse.class);

        try{
            String jsonResponse = restTemplate.getForObject(url, String.class);

            // 3. 받아온 String을 ObjectMapper를 사용해 수동으로 DTO 객체로 변환합니다.
            ObjectMapper objectMapper = new ObjectMapper();
            LibraryDto.Data4LibResponse response = objectMapper.readValue(jsonResponse, LibraryDto.Data4LibResponse.class);

            if (response == null || response.getResponse() == null || response.getResponse().getLibs() == null) {
                log.error("도서관 정보나루 API에서 데이터를 가져오는 데 실패했습니다.");
                return;
            }

            List<LibraryDto.Data4LibResponse.LibData> data4LibList = response.getResponse().getLibs().stream()
                    .map(lib -> lib.getLib())
                    .toList();
            log.info("정보나루 API로부터 {}개의 참여 도서관 목록을 가져왔습니다.", data4LibList.size());

            // 3. '정확한 일치' 전략으로 두 데이터를 연결합니다.
            //    (DB의 모든 도서관을 기준으로, 정보나루 목록에 일치하는 것이 있는지 찾아봅니다.)
            Map<String, LibraryDto.Data4LibResponse.LibData> data4LibMap = data4LibList.stream()
                    .collect(Collectors.toMap(
                            lib -> normalizeName(lib.getLibName()) + normalizeAddress(lib.getAddress()), // Key 생성
                            lib -> lib,
                            (existing, replacement) -> existing // 중복 Key 발생 시 기존 값 유지
                    ));

            List<Library> allLibrariesInDB = libraryRepository.findAll();
            log.info("DB에서 총 {}개의 도서관을 연결 대상으로 조회했습니다.", allLibrariesInDB.size());

            int successCount = 0;
            int failureCount = 0;
            int alreadyDoneCount = 0;

            for(Library dbLibrary : allLibrariesInDB){
                if (dbLibrary.getD4lLibCode() != null) {
                    alreadyDoneCount++;
                    continue;
                }

                String searchKey = normalizeName(dbLibrary.getLibName()) + normalizeAddress(dbLibrary.getAddress());
                LibraryDto.Data4LibResponse.LibData matchedLib = data4LibMap.get(searchKey);

                if (matchedLib != null) {
                    // 일치하는 도서관을 찾았으면, setter를 이용해 d4lLibCode를 업데이트합니다.
                    dbLibrary.setD4lLibCode(matchedLib.getLibCode());
                    log.info("✅ 매칭 성공: '{}' (d4l_lib_code: {})", dbLibrary.getLibName(), dbLibrary.getD4lLibCode());
                    successCount++;
                } else {
                    log.warn("❌ 매칭 실패: '{}' ({})", dbLibrary.getLibName(), dbLibrary.getAddress());
                    failureCount++;
                }
            }
            log.info("데이터 연결 작업 완료. 성공: {}, 실패: {}", successCount, failureCount);
        }catch (Exception e) {
            log.error("도서관 정보나루 데이터 처리 중 예외 발생", e);
        }
    }

    /**
     * [개선된 버전] 도서관 이름의 불필요한 부분을 제거하고 핵심 이름만 추출하여 정규화합니다.
     * @param name 원본 도서관 이름
     * @return 정규화된 도서관 이름
     */
    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        // 단계별로 정규화를 진행합니다.
        return name.trim() // 1. 이름 앞뒤의 공백을 먼저 제거합니다. (' 오현고등학교 ' 같은 케이스 처리)
                .replaceAll("\\(.*?\\)", "") // 2. 괄호와 그 안의 내용 제거 (예: "A도서관(본관)")
                .replaceAll("(?i)library|도서실|문화관|정보관|자료실", "도서관") // 3. '도서관'과 유사한 단어들을 통일시킵니다. (영문 대소문자 무시)
                .replaceAll("작은도서관|어린이도서관", "도서관") // 4. 특정 타입의 도서관을 일반 명칭으로 변경합니다.
                .replaceAll("[^가-힣a-zA-Z0-9]", ""); // 5. 마지막으로 한글, 영문, 숫자를 제외한 모든 특수문자와 공백을 제거합니다.
    }

    /**
     * [개선된 버전] 주소에서 핵심 행정구역(시/군/구, 읍/면/동)을 추출하여 정규화합니다.
     * 정규표현식을 사용하여 다양한 주소 형식에 대응합니다.
     * @param fullAddress 원본 전체 주소
     * @return 정규화된 핵심 주소
     */
    private String normalizeAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            return "";
        }

        // 대한민국 주소 체계를 처리하기 위한 정규표현식
        // (세종|제주|강원)특별자치(시|도), (서울|부산 등)광역/특별시, (경기|충북 등)도 등을 처리하고
        // 그 뒤에 오는 시/군/구 및 읍/면/동을 추출합니다.
        String regex = "([가-힣]+(특별시|광역시|특별자치시|도|특별자치도))?" + // 1. 시/도 (선택)
                "\\s*([가-힣]+(시|군|구))" +                  // 2. 시/군/구 (필수)
                "(\\s*[가-힣]+(읍|면|동))?";                 // 3. 읍/면/동 (선택)

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fullAddress.trim());

        StringBuilder coreAddress = new StringBuilder();
        if (matcher.find()) {
            // 정규식에 매칭되는 그룹들을 합칩니다.
            // group(1): 시/도, group(3): 시/군/구, group(5): 읍/면/동
            if (matcher.group(1) != null) {
                coreAddress.append(matcher.group(1)); // "경기도"
            }
            if (matcher.group(3) != null) {
                coreAddress.append(matcher.group(3)); // "성남시"
            }
            if (matcher.group(5) != null) {
                coreAddress.append(matcher.group(5)); // "분당구" 또는 "조천읍"
            }
        }

        if (coreAddress.isEmpty()) {
            // 정규식으로 핵심 주소를 찾지 못한 경우, 최소한의 정규화만 수행하여 반환 (Fallback 로직)
            return fullAddress.replaceAll("[^가-힣a-zA-Z0-9]", "");
        }

        // 추출된 핵심 주소에서 모든 공백과 특수문자를 최종적으로 제거합니다.
        return coreAddress.toString().replaceAll("[^가-힣a-zA-Z0-9]", "");
    }
}
