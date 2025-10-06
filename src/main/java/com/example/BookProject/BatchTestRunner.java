package com.example.BookProject;

import com.example.BookProject.service.LibraryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 실행 시점에 배치 작업을 수동으로 딱 한번 실행하기 위한 테스트용 클래스.
 * 테스트가 끝나면 이 파일을 삭제하거나 @Component를 주석 처리해주세요.
 */
@Slf4j
@Component // Spring이 이 클래스를 찾아서 실행할 수 있도록 등록합니다.
@RequiredArgsConstructor
public class BatchTestRunner implements CommandLineRunner {

    private final LibraryBatchService libraryBatchService;

    @Override
    public void run(String... args) throws Exception {
        log.info("BatchTestRunner를 사용하여 전체 도서관 데이터 파이프라인을 실행합니다.");
        // --- Step 1: NLSS 전국 도서관 데이터 수집 ---
        // DB에 데이터가 이미 있다면 이 부분은 주석 처리하고 실행해도 됩니다.
        log.info("--- Step 1 시작: NLSS 전국 도서관 데이터 수집 ---");
        libraryBatchService.fetchAndSaveLibraries();
        log.info("--- Step 1 종료: NLSS 전국 도서관 데이터 수집 ---");

        // --- Step 2: 정보나루 API와 데이터 연결 ---
        log.info("--- Step 2 시작: 도서관 정보나루 데이터 연결 ---");
        libraryBatchService.reconcileWithData4Lib();
        log.info("--- Step 2 종료: 도서관 정보나루 데이터 연결 ---");

        log.info("전체 도서관 데이터 파이프라인 실행 완료.");
    }
}