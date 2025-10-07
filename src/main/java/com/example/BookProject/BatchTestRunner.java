// com/example/BookProject/BatchTestRunner.java

package com.example.BookProject;

import com.example.BookProject.service.LibraryBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 실행 시점에 '정보나루'의 전체 도서관 데이터를 DB에 저장하기 위한 실행 클래스입니다.
 * 최초 1회 실행하여 DB에 데이터를 모두 저장한 후에는 이 파일을 삭제하거나 @Component를 주석 처리하는 것을 권장합니다.
 */
@Slf4j
@Component // Spring이 이 클래스를 찾아서 실행할 수 있도록 등록합니다.
@RequiredArgsConstructor
public class BatchTestRunner implements CommandLineRunner {

    //private final LibraryBatchService libraryBatchService;
    private final ExcelDataProcessor excelDataProcessor;

    @Override
    public void run(String ...args) throws Exception {
        log.info("BatchTestRunner를 사용하여 엑셀 파일 Dry Run을 시작합니다.");
        String xlsxFilePath = "C:/Users/cyhong/Downloads/국가자료종합목록.xlsx";
        excelDataProcessor.runDryRunDebugger(xlsxFilePath);

        log.info("Xlsx 파일 Dry Run을 완료했습니다.");
    }
/*
    @Override
    public void run(String... args) throws Exception {
        log.info("BatchTestRunner를 사용하여 '정보나루' 전체 도서관 데이터 저장 작업을 시작합니다.");

        // '정보나루' API를 호출하여 전체 도서관 정보를 DB에 저장하는 단일 메서드를 호출합니다.
        libraryBatchService.fetchAndSaveAllLibrariesFromData4Lib();

        log.info("'정보나루' 전체 도서관 데이터 저장 작업을 완료했습니다.");
    }*/
}