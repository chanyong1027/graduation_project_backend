package com.example.BookProject;

import com.example.BookProject.domain.Library;
import com.example.BookProject.dto.KakaoApiResponseDto;
import com.example.BookProject.repository.LibraryRepository;
import com.example.BookProject.service.KakaoApiService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance; // <-- Levenshtein import
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelDataProcessor {

    private final LibraryRepository libraryRepository;
    private final KakaoApiService kakaoApiService;

    public void runRealRun(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("debug_output.txt"))) {
            System.out.println("--- Real Run 시작: 신규 도서관을 DB에 저장합니다. ---");

            Map<String, List<Library>> existingLibrariesByAddress = libraryRepository.findAll().stream()
                    .collect(Collectors.groupingBy(lib -> normalizeAddress(lib.getAddress())));

            System.out.println("DB에서 " + existingLibrariesByAddress.size() + "개의 고유한 주소 키를 로드했습니다.");

            List<Library> newLibrariesToSave = new ArrayList<>();

            try (FileInputStream file = new FileInputStream(filePath);
                 Workbook workbook = new XSSFWorkbook(file)) {

                Sheet sheet = workbook.getSheetAt(0);
                for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    Cell nameCell = row.getCell(3);
                    Cell addressCell = row.getCell(6);
                    Cell homePageCell = row.getCell(5);

                    if (nameCell != null && addressCell != null && !nameCell.getStringCellValue().isBlank()) {
                        String libName = nameCell.getStringCellValue();
                        String address = addressCell.getStringCellValue();
                        String newNormalizedName = normalizeName(libName);
                        String newNormalizedAddress = normalizeAddress(address);

                        if (newNormalizedAddress.isEmpty()) continue;

                        boolean isDuplicate = false;
                        List<Library> candidatesInSameAddress = existingLibrariesByAddress.get(newNormalizedAddress);

                        if (candidatesInSameAddress != null) {
                            for (Library candidate : candidatesInSameAddress) {
                                String candidateNormalizedName = normalizeName(candidate.getLibName());
                                double score = calculateOverallSimilarity(newNormalizedName, candidateNormalizedName, address, candidate.getAddress());
                                if (score > 0.7) { // 최종 임계값
                                    isDuplicate = true;
                                    break;
                                }
                            }
                        }

                        if (!isDuplicate) {
                            String homepage = (homePageCell != null && homePageCell.getCellType() == CellType.STRING) ? homePageCell.getStringCellValue() : null;

                            // --- 지오코딩 API 호출 ---
                            String refinedAddress = refineAddressForGeocoding(address);
                            KakaoApiResponseDto.Document coords = kakaoApiService.getCoordinates(refinedAddress);
                            Double latitude = (coords != null) ? coords.getLatitude() : null;
                            Double longitude = (coords != null) ? coords.getLongitude() : null;
                            // ------------------------

                            if (latitude == null || longitude == null) {
                                System.out.println("[좌표 변환 실패] " + libName + " / " + address);
                                continue; // 좌표가 없으면 저장하지 않음
                            }

                            Library newLibrary = Library.builder()
                                    .libName(libName)
                                    .address(address)
                                    .homepage(homepage)
                                    .latitude(latitude)
                                    .longitude(longitude)
                                    .build();
                            newLibrariesToSave.add(newLibrary);
                        }
                    }
                }
            }

            if (!newLibrariesToSave.isEmpty()) {
                libraryRepository.saveAll(newLibrariesToSave);
            }

            System.out.println("\n--- Real Run 종료 ---");
            System.out.println(">>> DB에 새로 저장된 도서관 개수: " + newLibrariesToSave.size() + "개");
        }
    }

    /**
     * [최종 개선] '도로명주소'가 일치하면 매우 높은 점수를 반환하는 로직 추가
     */
    private double calculateOverallSimilarity(String name1, String name2, String fullAddress1, String fullAddress2) {
        // 1. 각 주소에서 '도로명주소 키'를 추출합니다.
        String streetKey1 = extractStreetAddressKey(fullAddress1);
        String streetKey2 = extractStreetAddressKey(fullAddress2);

        // 2. 두 키가 존재하고, 서로 완벽히 일치하면 0.95점 부여
        if (!streetKey1.isEmpty() && streetKey1.equals(streetKey2)) {
            return 0.95;
        }

        // 3. 일치하지 않으면 이전의 가중 평균 방식 사용
        double nameScore = calculateCombinedSimilarity(name1, name2);
        double addressScore = calculateCombinedSimilarity(normalizeForSimilarity(fullAddress1), normalizeForSimilarity(fullAddress2));
        return (nameScore * 0.3) + (addressScore * 0.7);
    }

    /**
     * [새로운 헬퍼] 정규표현식을 사용하여 주소에서 '도로명 + 건물번호' 패턴을 추출합니다.
     */
    private String extractStreetAddressKey(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) return "";

        // 예: '노해로69길 151', '사직로9길 15-14' 같은 패턴을 찾음
        String regex = "([가-힣0-9]+(로|길|대로))\\s*([0-9-]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fullAddress);

        if (matcher.find()) {
            // group(1) = 도로명, group(3) = 건물번호
            String streetName = matcher.group(1);
            String streetNumber = matcher.group(3);
            return (streetName + streetNumber).replaceAll("[^가-힣a-zA-Z0-9]", "");
        }

        return ""; // 패턴을 찾지 못하면 빈 문자열 반환
    }

    /**
     * [새로운 헬퍼] 주소의 앞 4 어절(시/도, 시/군/구, 도로명, 건물번호)을 추출하여 핵심 키 생성
     */
  /*  private String getCoreAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) return "";
        String addressWithoutParentheses = fullAddress.replaceAll("\\(.*?\\)", "").trim();
        String[] parts = addressWithoutParentheses.split(" ");
        if (parts.length >= 4) {
            return (parts[0] + parts[1] + parts[2] + parts[3]).replaceAll("[^가-힣a-zA-Z0-9]", "");
        }
        return ""; // 4 어절이 안되면 비교에 사용하지 않음
    }*/

    // 유사도 비교를 위해 주소에서 공백과 특수문자만 제거하는 간단한 정규화
    private String normalizeForSimilarity(String text) {
        if (text == null || text.isBlank()) return "";
        return text.replaceAll("[^가-힣a-zA-Z0-9]", "");
    }

    private double calculateCombinedSimilarity(String str1, String str2) {
        double jaroScore = new JaroWinklerSimilarity().apply(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        if (maxLength == 0) return 1.0;
        int levenshteinDistance = new LevenshteinDistance().apply(str1, str2);
        double levenshteinSimilarity = 1.0 - (double) levenshteinDistance / maxLength;
        return (jaroScore * 0.4) + (levenshteinSimilarity * 0.6);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return "";
        return name.trim()
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("(도서관|자료실|정보관|분관|본관|문고|북카페)$", "")
                .replaceAll("[^가-힣a-zA-Z0-9]", "");
    }

    private String normalizeAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) return "";
        String trimmedAddress = fullAddress.trim();
        if (trimmedAddress.startsWith("세종특별자치시")) return "세종특별자치시";
        String regex = "([가-힣]+(특별시|광역시|특별자치시|도|특별자치도))?\\s*([가-힣]+(시|군|구))(\\s*[가-힣]+(읍|면|동))?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(trimmedAddress);
        StringBuilder coreAddress = new StringBuilder();
        if (matcher.find()) {
            if (matcher.group(1) != null) coreAddress.append(matcher.group(1));
            if (matcher.group(3) != null) coreAddress.append(matcher.group(3));
            if (matcher.group(5) != null) coreAddress.append(matcher.group(5));
        }
        if (coreAddress.isEmpty()) {
            String[] parts = trimmedAddress.split(" ");
            if (parts.length >= 2) return (parts[0] + parts[1]).replaceAll("[^가-힣a-zA-Z0-9]", "");
            return trimmedAddress.replaceAll("[^가-힣a-zA-Z0-9]", "");
        }
        return coreAddress.toString().replaceAll("[^가-힣a-zA-Z0-9]", "");
    }

    /**
     * 지오코딩 API가 주소를 더 잘 인식하도록 불필요한 부분을 제거합니다.
     */
    private String refineAddressForGeocoding(String address) {
        if (address == null) return null;
        // 괄호와 그 안의 내용 제거
        String refined = address.replaceAll("\\(.*?\\)", "");
        // ' 내', ' 일대' 등 부가 설명 제거
        refined = refined.replaceAll("\\s+내$|\\s+일대$|\\s+주민센타", "");
        return refined.trim();
    }
}