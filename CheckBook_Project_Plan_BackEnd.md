# CheckBook 백엔드 진행 상황 요약

## 1. 구현 완료 항목

### 1.1 도메인 & 저장소
- 도서, 사용자, 도서관, 독서 기록, 리뷰 등 핵심 엔티티와 JPA 레포지토리 구성 완료
- 위치: `src/main/java/com/example/BookProject/domain`, `repository`

### 1.2 인증·인가
- 회원가입/로그인/로그아웃/탈퇴 API 구현 완료
- JWT 발급 및 Spring Security 필터 체인으로 보호된 엔드포인트 구축
- 위치: `controller/UserController.java`, `jwt`, `config/SecurityConfig.java`

### 1.3 사용자 프로필 관리 (P0 요건 충족)
- **기본 위치/반경 설정**: `User` 엔티티에 `homeLatitude`, `homeLongitude`, `radiusKm` 필드 추가 (기본값 5km)
- **프로필 수정 API**: `PATCH /api/users/profile` (닉네임, 나이, 성별, 이미지)
- **위치 설정 API**: `PATCH /api/users/location` (위도, 경도, 반경 1~20km)
- **내 정보 조회 API**: `GET /api/users/me`
- **로그아웃 API**: `POST /api/users/logout`
- 위치: `domain/User.java:46-53`, `controller/UserController.java:106-143`

### 1.4 도서 검색 기능
- 알라딘 API 연동을 통한 검색/상세/베스트셀러/신간 조회
- 검색어 캐싱 구현
- 위치: `service/BookService.java`

### 1.5 도서관 기능 (P0 요건 충족)
- 좌표 기반 근처 도서관 조회 (Haversine formula)
- 정보나루 API를 활용한 소장/대출 상태 확인
- **내 도서관 최대 3개 제한**: `LibraryService.java:292-294`에서 검증 로직 구현
- **내 도서관 우선 노출**: `getLibraryBookStatuses()` 메서드에서 `isFavorite` 플래그 기반 정렬
- 내 도서관 등록/조회/삭제 API 구현
- 위치: `service/LibraryService.java`, `controller/LibraryController.java`

### 1.6 도서 상세 가용성
- 도서관 소장/대출 정보 제공: `GET /api/libraries/book-status`
- 내 도서관 우선 노출 및 정렬 구현
- 위치: `LibraryService.java:204-272`, `LibraryController.java:76-87`

### 1.7 독서 기록 관리
- 내 서재 추가, 상태 변경 (찜/읽는 중/완독)
- 별점·리뷰 저장/수정/삭제 API 제공
- JWT 기반 인증 처리 개선 완료
- 위치: `controller/BookRecordController.java`, `service/BookRecordService.java`

### 1.8 리뷰 시스템 (완전 재구성 완료)
- **ISBN 기반 리뷰 작성/조회**: 책 ISBN으로 리뷰 식별 및 관리
- **사용자별 리뷰 조회**: 내가 작성한 모든 리뷰 조회 (`GET /api/reviews/me`)
- **권한 관리**: 작성자 본인만 수정/삭제 가능
- **중복 방지**: 동일 책에 대해 사용자당 1개의 리뷰만 작성 가능
- **프론트엔드 요구사항 완벽 대응**: `reference/ReviewPage.tsx` 기준으로 재설계
- 위치: `controller/ReviewController.java`, `service/ReviewService.java`, `repository/ReviewRepository.java`

### 1.9 보안
- **모든 외부 API 키 환경변수 처리 완료**:
  - `DB_PASSWORD`, `JWT_SECRET`, `ALADIN_API_KEY`, `DATA4LIBRARY_API_KEY`, `KAKAO_API_KEY`
  - 위치: `application.yml:7,20,29-31`

## 2. 남은 리스크 및 개선 사항

### 2.1 최종 수정 완료 (2025-01-11)
- ✅ `UserRepository.findByUserName()` → `findByUserNm()`로 수정
- ✅ `BookRecordReviewUpdateRequestDto` 신규 생성 (BookRecord용 review/rating 업데이트)
- ✅ `BookRecordService` 및 `BookRecordController`에서 올바른 DTO 사용하도록 수정

### 2.2 테스트 부재
- `src/test/java`에 단위 테스트 및 통합 테스트 부족
- 외부 API 연동 및 도메인 로직의 회귀 검증 수단 필요

### 2.3 에러 핸들링 개선 필요
- 전역 예외 처리는 `GlobalExceptionHandler.java`에 구현되어 있으나, 더 세분화된 예외 메시지 필요

## 3. API 엔드포인트 요약

### 3.1 사용자 관리
- `POST /api/users/register` - 회원가입
- `POST /api/users/login` - 로그인
- `POST /api/users/logout` - 로그아웃 ✅ 신규
- `GET /api/users/me` - 내 정보 조회 ✅ 신규
- `PATCH /api/users/profile` - 프로필 수정 ✅ 신규
- `PATCH /api/users/location` - 위치/반경 설정 ✅ 신규
- `DELETE /api/users/{id}` - 회원 탈퇴

### 3.2 도서관 관리
- `GET /api/libraries/search` - 근처 도서관 검색
- `GET /api/libraries/book-status` - 도서 소장/대출 상태 조회 (내 도서관 우선)
- `POST /api/libraries/{libraryId}/my-library` - 내 도서관 추가 (최대 3개)
- `GET /api/libraries/my-library` - 내 도서관 목록
- `DELETE /api/libraries/{libraryId}/my-library` - 내 도서관 삭제

### 3.3 도서 검색 & 독서 기록
- `GET /api/books/search` - 도서 검색
- `GET /api/books/detail/{isbn}` - 도서 상세
- `GET /api/books/bestsellers` - 베스트셀러
- `GET /api/books/new-releases` - 신간
- BookRecord 관련 CRUD API

### 3.4 리뷰 관리 ✅ 신규
- `POST /api/reviews` - 리뷰 생성 (ISBN 기반)
- `GET /api/reviews/books/{isbn}` - 특정 책의 모든 리뷰 조회
- `GET /api/reviews/me` - 내가 작성한 모든 리뷰 조회
- `GET /api/reviews/{reviewId}` - 특정 리뷰 단건 조회
- `PATCH /api/reviews/{reviewId}` - 리뷰 수정 (본인만)
- `DELETE /api/reviews/{reviewId}` - 리뷰 삭제 (본인만)

## 4. 권장 후속 작업
1. ✅ ~~사용자 프로필·내 도서관 정책(P0 필수) 구현~~ → **완료**
2. ✅ ~~리뷰 API 경로 중복 및 repository 호출 오류 수정~~ → **완료**
3. ✅ ~~도서 상세 응답에 주변 도서관 가용성과 내 도서관 우선 정렬 통합~~ → **완료**
4. ✅ ~~외부 API 키를 환경 변수로 이전~~ → **완료**
5. 핵심 서비스와 외부 연동을 대상으로 최소 단위·통합 테스트 작성
6. 프로덕션 배포를 위한 성능 최적화 및 모니터링 설정

## 5. 리뷰 시스템 상세 설명

### 5.1 설계 철학
- **BookRecord vs Review 분리**
  - `BookRecord`: 개인 독서 기록 (찜/읽는 중/완독, 개인 메모)
  - `Review`: 공개 리뷰 (다른 사용자와 공유, ISBN 기반)

### 5.2 주요 기능
- **ISBN 기반 식별**: 프론트엔드에서 주로 ISBN을 사용하므로 API도 ISBN 기반
- **중복 방지**: 사용자당 책마다 1개의 리뷰만 작성 가능
- **권한 검증**: 수정/삭제 시 작성자 본인 여부 확인
- **유저 이메일 기반 인증**: `@AuthenticationPrincipal UserDetails`로 userEmail 추출

### 5.3 API 응답 구조
```json
{
  "reviewId": 1,
  "content": "리뷰 내용",
  "reviewImg": "이미지 URL",
  "rating": 4.5,
  "authorNickname": "사용자닉네임",
  "authorId": 123,
  "bookId": 456,
  "bookIsbn": "9788937460890",
  "bookTitle": "책 제목",
  "createdAt": "2025-01-01T12:00:00",
  "updatedAt": "2025-01-02T12:00:00"
}
```

---

## 6. 프론트엔드 통합 준비 완료 (2025-01-11)

### 6.1 Refresh Token 기반 인증 시스템 구현 ✅

#### 새로 추가된 파일
- `domain/RefreshToken.java` - Refresh Token 엔티티 (DB 저장)
- `repository/RefreshTokenRepository.java` - Refresh Token CRUD
- `service/TokenService.java` - Token 생성/검증/삭제 로직
- `dto/ApiResponse.java` - API 응답 표준화 DTO

#### 수정된 파일
- `jwt/JwtTokenProvider.java` - Refresh Token 생성/검증 메서드 추가
- `controller/UserController.java` - 로그인/로그아웃/Refresh API 수정
- `dto/UserDto.java` - LoginResponse, TokenRefreshRequest/Response 추가
- `config/SecurityConfig.java` - CORS 설정 및 `/refresh` 경로 허용
- `application.yml` - Refresh Token 유효 기간 설정 (7일)

#### 주요 기능
1. **로그인 시 Token 발급**
   - Access Token (1시간) + Refresh Token (7일) 동시 발급
   - Refresh Token은 DB에 저장 (Redis 미사용)

2. **로그아웃 시 Token 무효화**
   - DB에서 사용자의 Refresh Token 삭제
   - SecurityContext 정리

3. **Token Refresh**
   - `POST /api/users/refresh`
   - Refresh Token으로 새로운 Access Token + Refresh Token 발급
   - Refresh Token Rotation 적용 (보안 강화)

4. **만료된 Token 자동 정리**
   - `TokenService.cleanupExpiredTokens()` 메서드 제공
   - 스케줄러로 주기적 실행 가능

### 6.2 CORS 설정 완료 ✅

#### 허용된 오리진
```yaml
- http://localhost:5173  # Vite 개발 서버
- http://localhost:3000  # React 개발 서버
- http://localhost:4173  # Vite 프리뷰 서버
```

#### 허용된 HTTP 메서드
- GET, POST, PUT, PATCH, DELETE, OPTIONS

#### 허용된 헤더
- Authorization (JWT 토큰)
- Content-Type
- X-Requested-With
- Accept
- Origin

#### 인증 정보
- `allowCredentials: true` (쿠키, Authorization 헤더 포함 가능)

### 6.3 API 응답 포맷 표준화 ✅

#### ApiResponse<T> 구조
```json
{
  "success": true,
  "message": "성공",
  "data": { ... },
  "timestamp": "2025-01-11T12:00:00"
}
```

#### 사용 방법
```java
// 성공
return ResponseEntity.ok(ApiResponse.success(data));

// 실패
return ResponseEntity.badRequest()
    .body(ApiResponse.error("에러 메시지"));
```

### 6.4 추가된 API 엔드포인트

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| `POST` | `/api/users/login` | 로그인 (Access + Refresh Token 발급) | ❌ |
| `POST` | `/api/users/logout` | 로그아웃 (Refresh Token 무효화) | ✅ |
| `POST` | `/api/users/refresh` | Token Refresh (새 토큰 발급) | ❌ |

### 6.5 데이터베이스 스키마 변경

#### 새로 추가된 테이블: `refresh_tokens`
```sql
CREATE TABLE refresh_tokens (
    token_id BIGSERIAL PRIMARY KEY,
    token_value VARCHAR(500) UNIQUE NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_refresh_token_value ON refresh_tokens(token_value);
CREATE INDEX idx_refresh_token_user_email ON refresh_tokens(user_email);
CREATE INDEX idx_refresh_token_expires_at ON refresh_tokens(expires_at);
```

### 6.6 프론트엔드 연동 가이드

#### 1. 로그인
```typescript
const response = await axios.post('/api/users/login', {
  userEmail: 'test@example.com',
  userPw: 'password123'
});

// 응답
{
  accessToken: "eyJhbGciOiJIUzUxMiJ9...",
  refreshToken: "eyJhbGciOiJIUzUxMiJ9...",
  userId: 1,
  userNm: "홍길동",
  userEmail: "test@example.com"
}

// localStorage에 저장
localStorage.setItem('accessToken', response.data.accessToken);
localStorage.setItem('refreshToken', response.data.refreshToken);
```

#### 2. API 요청 (Axios Interceptor)
```typescript
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

#### 3. Token Refresh (401 에러 시)
```typescript
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      
      try {
        const response = await axios.post('/api/users/refresh', {
          refreshToken
        });
        
        // 새 토큰 저장
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        
        // 원래 요청 재시도
        error.config.headers.Authorization = 
          `Bearer ${response.data.accessToken}`;
        return axios.request(error.config);
      } catch (refreshError) {
        // Refresh 실패 시 로그아웃
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
```

#### 4. 로그아웃
```typescript
await axios.post('/api/users/logout', {}, {
  headers: {
    Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  }
});

localStorage.clear();
window.location.href = '/login';
```

### 6.7 보안 강화 사항

1. **Refresh Token Rotation**
   - Token Refresh 시마다 새로운 Refresh Token 발급
   - 기존 Refresh Token은 무효화
   - Replay Attack 방지

2. **DB 기반 Token 관리**
   - Redis 없이 PostgreSQL 사용
   - 로그아웃 시 즉시 무효화 가능
   - 만료된 Token 자동 정리

3. **Token 유효 기간**
   - Access Token: 1시간 (짧은 유효 기간)
   - Refresh Token: 7일 (주기적 재로그인 유도)

4. **CORS 정책**
   - 특정 오리진만 허용
   - Credentials 포함 허용
   - Preflight 캐싱

### 6.8 다음 단계

#### 프론트엔드 작업
- [ ] Axios Interceptor 설정
- [ ] Token 저장/관리 로직 구현
- [ ] 401 에러 핸들링
- [ ] 자동 Token Refresh

#### 백엔드 작업 (선택)
- [ ] Token 만료 정리 스케줄러 추가
- [ ] Rate Limiting (로그인 시도 제한)
- [ ] IP 기반 Refresh Token 검증
- [ ] Device 관리 (멀티 디바이스 지원)

---

**작성일**: 2025-01-11  
**작성자**: Claude Code  
**문서 버전**: 2.0 (프론트엔드 통합 준비 완료)
