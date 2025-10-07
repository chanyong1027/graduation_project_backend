package com.example.BookProject;

import com.example.BookProject.domain.Library;
import com.example.BookProject.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance; // <-- Levenshtein import
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelDataProcessor {

    private final LibraryRepository libraryRepository;

    public void runDryRunDebugger(String filePath) throws IOException {
        System.out.println("--- [최종 디버깅 Dry Run] 시작: 중복 실패 후보를 확인합니다. ---");

        Map<String, Library> existingLibraryMap = libraryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        library -> normalizeName(library.getLibName()) + "|" + normalizeAddress(library.getAddress()),
                        library -> library,
                        (existing, replacement) -> existing
                ));
        System.out.println("DB에서 " + existingLibraryMap.size() + "개의 기존 도서관 키를 로드했습니다.");

        int duplicateCount = 0;
        int newLibraryCandidateCount = 0;

        try (FileInputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell nameCell = row.getCell(4);
                Cell addressCell = row.getCell(7);

                if (nameCell != null && addressCell != null) {
                    String libName = nameCell.getStringCellValue();
                    String address = addressCell.getStringCellValue();
                    String newNormalizedName = normalizeName(libName);
                    String newNormalizedAddress = normalizeAddress(address);

                    if (newNormalizedName.isEmpty() || newNormalizedAddress.isEmpty()) continue;

                    double bestMatchScore = 0.0;
                    Library bestMatchLibrary = null;

                    // 엑셀 도서관 하나에 대해 가장 비슷한 DB 도서관 '하나'만 찾는 로직
                    for (Map.Entry<String, Library> entry : existingLibraryMap.entrySet()) {
                        String[] existingKeyParts = entry.getKey().split("\\|");
                        String existingNormalizedName = existingKeyParts[0];
                        String existingNormalizedAddress = existingKeyParts.length > 1 ? existingKeyParts[1] : "";

                        if (newNormalizedAddress.equals(existingNormalizedAddress)) {
                            double combinedScore = calculateCombinedSimilarity(newNormalizedName, existingNormalizedName);
                            if (combinedScore > bestMatchScore) { // 최고 점수를 계속 갱신
                                bestMatchScore = combinedScore;
                                bestMatchLibrary = entry.getValue();
                            }
                        }
                    }

                    // 루프가 끝난 후, 찾은 '최고 점수'를 기준으로 판단 및 출력
                    if (bestMatchScore > 0.90) { // 임계값 0.90
                        duplicateCount++;
                    } else {
                        newLibraryCandidateCount++;
                        if (bestMatchLibrary != null) { // 가장 비슷했던 후보가 있다면 출력
                            System.out.println("\n[중복 실패 후보] 최종 점수: " + String.format("%.2f", bestMatchScore));
                            System.out.println("  - [엑셀] 이름: " + libName + " (키: " + newNormalizedName + ")");
                            System.out.println("  - [DB]   이름: " + bestMatchLibrary.getLibName() + " (키: " + normalizeName(bestMatchLibrary.getLibName()) + ")");
                            System.out.println("  (공통 주소 키: " + newNormalizedAddress + ")");
                        }
                    }
                }
            }
        }
        System.out.println("\n--- 디버깅 Dry Run 종료 ---");
        System.out.println(">>> 중복으로 판단된 도서관 개수: " + duplicateCount + "개");
        System.out.println(">>> 신규 도서관으로 추정되는 개수: " + newLibraryCandidateCount + "개");
    }

    /**
     * Jaro-Winkler와 Levenshtein 점수를 조합하여 최종 유사도 점수를 계산합니다.
     */
    private double calculateCombinedSimilarity(String str1, String str2) {
        // 1. Jaro-Winkler 점수 (오타, 순서에 강함)
        double jaroScore = new JaroWinklerSimilarity().apply(str1, str2);

        // 2. Levenshtein 점수 (글자 수 차이에 민감)
        int maxLength = Math.max(str1.length(), str2.length());
        if (maxLength == 0) return 1.0;
        int levenshteinDistance = new LevenshteinDistance().apply(str1, str2);
        double levenshteinSimilarity = 1.0 - (double) levenshteinDistance / maxLength;

        // 3. 두 점수의 평균을 최종 점수로 사용 (가중치 조절 가능)
        return (jaroScore + levenshteinSimilarity) / 2.0;
    }

    /**
     * [개선된 버전] 이름의 고유성을 살리기 위해 지역명은 제거하지 않습니다.
     */
    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.trim()
                .replaceAll("\\(.*?\\)", "") // 괄호와 그 안의 내용 제거
                .replaceAll("도서관|자료실|정보관|분관|본관|작은|시립|구립|군립|국립|도립", "") // 일반적인 단어만 제거
                .replaceAll("[^가-힣a-zA-Z0-9]", ""); // 특수문자/공백 제거
    }
    /**
     * [개선된 버전] 주소에서 '읍/면/동'까지 추출하여 더 상세한 키를 만듭니다.
     */
    private String normalizeAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            return "";
        }
        String trimmedAddress = fullAddress.trim();

        if (trimmedAddress.startsWith("세종특별자치시")) {
            return "세종특별자치시"; // 세종시는 예외 처리
        }

        // '시/도', '시/군/구', '읍/면/동'을 추출하는 정규표현식
        String regex = "([가-힣]+(특별시|광역시|특별자치시|도|특별자치도))?" + // 시/도 (선택)
                "\\s*([가-힣]+(시|군|구))" +                  // 시/군/구 (필수)
                "(\\s*[가-힣]+(읍|면|동))?";                 // 읍/면/동 (선택)

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(trimmedAddress);

        StringBuilder coreAddress = new StringBuilder();
        if (matcher.find()) {
            if (matcher.group(1) != null) coreAddress.append(matcher.group(1)); // 시/도
            if (matcher.group(3) != null) coreAddress.append(matcher.group(3)); // 시/군/구
            if (matcher.group(5) != null) coreAddress.append(matcher.group(5)); // 읍/면/동
        }

        if (coreAddress.isEmpty()) {
            // 정규식 실패 시 대체 로직
            String[] parts = trimmedAddress.split(" ");
            if (parts.length >= 2) return (parts[0] + parts[1]).replaceAll("[^가-힣a-zA-Z0-9]", "");
            return trimmedAddress.replaceAll("[^가-힣a-zA-Z0-9]", "");
        }
        return coreAddress.toString().replaceAll("[^가-힣a-zA-Z0-9]", "");
    }
}

