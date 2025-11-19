package com.example.BookProject.controller;

import com.example.BookProject.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * 이메일 인증코드 전송
     *
     * @param request { "email": "user@example.com" }
     * @return { "message": "인증코드가 전송되었습니다." }
     */
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // 이메일 유효성 검사
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "이메일을 입력해주세요."));
        }

        // 간단한 이메일 형식 검증
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "올바른 이메일 형식이 아닙니다."));
        }

        try {
            emailService.sendVerificationEmail(email);
            log.info("인증코드 전송 요청 성공: {}", email);
            return ResponseEntity.ok(Collections.singletonMap("message", "인증코드가 전송되었습니다."));
        } catch (Exception e) {
            log.error("인증코드 전송 실패: {}", email, e);
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("message", "이메일 발송에 실패했습니다. 다시 시도해주세요."));
        }
    }

    /**
     * 이메일 인증코드 검증
     *
     * @param request { "email": "user@example.com", "code": "123456" }
     * @return { "verified": true } 또는 { "verified": false, "message": "인증 실패 사유" }
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        // 입력값 검증
        if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "verified", false,
                            "message", "이메일과 인증코드를 모두 입력해주세요."
                    ));
        }

        // 인증코드 검증
        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            log.info("이메일 인증 성공: {}", email);
            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "message", "이메일 인증이 완료되었습니다."
            ));
        } else {
            log.warn("이메일 인증 실패: {}", email);
            return ResponseEntity.ok(Map.of(
                    "verified", false,
                    "message", "인증코드가 일치하지 않거나 만료되었습니다."
            ));
        }
    }
}
