# BookProject Backend 문서

## 프로젝트 개요

독서 기록 및 도서관 정보를 제공하는 Spring Boot 백엔드 애플리케이션입니다.

### 기술 스택
- **Java**: 17
- **Spring Boot**: 3.4.4
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Security**: Spring Security + JWT
- **빌드 도구**: Gradle

### 주요 의존성
- Spring Boot Starter (Web, JPA, Security, Validation)
- PostgreSQL Driver
- Lombok
- JWT (jjwt 0.11.5)
- Jsoup (웹 크롤링)
- Apache POI (Excel 처리)
- H2 Database (테스트용)

---

## 데이터베이스 설정 및 연동

### 1. PostgreSQL 설치 및 설정

#### Windows/WSL 환경에서 PostgreSQL 설정

1. **PostgreSQL 설치 확인**
   ```bash
   psql --version
   ```

2. **PostgreSQL 서비스 시작** (WSL의 경우)
   ```bash
   sudo service postgresql start
   ```

3. **PostgreSQL 접속**
   ```bash
   sudo -u postgres psql
   ```

### 2. 데이터베이스 및 사용자 생성

PostgreSQL에 접속한 후 다음 명령어를 실행합니다:

```sql
-- 데이터베이스 생성
CREATE DATABASE "BookProject";

-- 사용자 생성
CREATE USER bookproject WITH PASSWORD '여기에_비밀번호_입력';

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE "BookProject" TO bookproject;

-- PostgreSQL 15 이상인 경우 추가 권한 부여
\c BookProject
GRANT ALL ON SCHEMA public TO bookproject;
```

생성 확인:
```sql
-- 데이터베이스 목록 확인
\l

-- 사용자 목록 확인
\du

-- 연결 종료
\q
```

### 3. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하거나, 시스템 환경 변수로 설정합니다:

```bash
# 데이터베이스 비밀번호
export DB_PASSWORD=여기에_비밀번호_입력

# JWT Secret (Base64 인코딩된 문자열)
export JWT_SECRET=여기에_랜덤_문자열_입력

# 외부 API 키
export ALADIN_API_KEY=알라딘_API_키
export DATA4LIBRARY_API_KEY=정보나루_API_키
export KAKAO_API_KEY=카카오_API_키
```

**JWT Secret 생성 방법:**
```bash
# Linux/Mac
openssl rand -base64 64

# 또는 온라인 도구 사용
# https://www.base64encode.org/
```

### 4. application.yml 설정

`src/main/resources/application.yml` 파일은 이미 다음과 같이 설정되어 있습니다:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/BookProject
    username: bookproject
    password: ${DB_PASSWORD}  # 환경 변수 사용

  jpa:
    hibernate:
      ddl-auto: update  # 테이블 자동 생성/업데이트
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: true

jwt:
  secret: ${JWT_SECRET}
  token-validity-in-seconds: 3600  # Access Token: 1시간
  refresh-token-validity-in-seconds: 604800  # Refresh Token: 7일

external:
  api:
    aladin: ${ALADIN_API_KEY}
    data4library: ${DATA4LIBRARY_API_KEY}
    kakao: ${KAKAO_API_KEY}
```

### 5. 연결 테스트

애플리케이션을 실행하여 데이터베이스 연결을 테스트합니다:

```bash
./gradlew bootRun
```

성공적으로 실행되면 콘솔에서 Hibernate가 테이블을 생성하는 SQL 로그를 확인할 수 있습니다.

---

## 정보나루 도서관 데이터 수집 방법

### 개요

정보나루(data4library) API를 통해 전국 도서관 정보를 수집하여 데이터베이스에 저장합니다.

### 1. API 키 발급

1. [정보나루](https://www.data4library.kr/) 웹사이트 접속
2. 회원가입 및 로그인
3. 마이페이지 > API 키 발급 메뉴에서 API 키 발급
4. 발급받은 API 키를 환경 변수에 설정:
   ```bash
   export DATA4LIBRARY_API_KEY=발급받은_API_키
   ```

### 2. 도서관 데이터 수집 방법

#### 방법 1: API 엔드포인트 호출 (수동)

Controller에 배치 작업을 트리거하는 엔드포인트를 추가하여 호출할 수 있습니다.

`UtilController.java`를 확인하거나 다음과 같이 직접 호출:

```java
// UtilController.java 또는 별도의 Controller에 추가
@PostMapping("/batch/libraries")
public ResponseEntity<String> fetchLibraries() {
    libraryBatchService.fetchAndSaveAllLibrariesFromData4Lib();
    return ResponseEntity.ok("도서관 데이터 수집이 완료되었습니다.");
}
```

그 후 Postman 또는 curl로 호출:
```bash
curl -X POST http://localhost:8080/api/batch/libraries
```

#### 방법 2: 애플리케이션 시작 시 자동 실행

`BookProjectApplication.java`에 다음과 같이 추가:

```java
@SpringBootApplication
@EnableScheduling
public class BookProjectApplication implements CommandLineRunner {

    private final LibraryBatchService libraryBatchService;

    public BookProjectApplication(LibraryBatchService libraryBatchService) {
        this.libraryBatchService = libraryBatchService;
    }

    public static void main(String[] args) {
        SpringApplication.run(BookProjectApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 주석 해제하여 시작 시 자동 실행
        // libraryBatchService.fetchAndSaveAllLibrariesFromData4Lib();
    }
}
```

#### 방법 3: 스케줄링 설정 (주기적 자동 실행)

`LibraryBatchService.java`에 `@Scheduled` 어노테이션 추가:

```java
@Scheduled(cron = "0 0 2 * * ?")  // 매일 새벽 2시 실행
public void scheduledFetchLibraries() {
    fetchAndSaveAllLibrariesFromData4Lib();
}
```

### 3. 배치 작업 동작 방식

`LibraryBatchService.java`의 `fetchAndSaveAllLibrariesFromData4Lib()` 메서드:

1. **페이징 처리**: 1,000개씩 페이지 단위로 데이터를 가져옴
2. **중복 체크**: 기존 DB의 `d4lLibCode`와 비교하여 중복 방지
3. **좌표 검증**: 위도/경도 정보가 없는 도서관은 제외
4. **일괄 저장**: 페이지별로 새로운 도서관 정보를 DB에 저장

```java
@Transactional
public void fetchAndSaveAllLibrariesFromData4Lib() {
    log.info("정보나루 API를 이용한 도서관 데이터 배치 작업을 시작합니다.");

    // 기존 도서관 코드 메모리 로드
    Set<Long> existingLibCodes = libraryRepository.findAll().stream()
        .map(Library::getD4lLibCode)
        .collect(Collectors.toSet());

    int pageNo = 1;
    final int pageSize = 1000;

    while (true) {
        String url = String.format(
            "http://data4library.kr/api/libSrch?authKey=%s&pageNo=%d&pageSize=%d&format=json",
            data4LibApiKey, pageNo, pageSize
        );

        // API 호출 및 저장 로직...
        pageNo++;
    }
}
```

### 4. 데이터 확인

PostgreSQL에서 데이터 확인:

```sql
-- PostgreSQL 접속
sudo -u postgres psql -d BookProject

-- 도서관 데이터 확인
SELECT COUNT(*) FROM libraries;
SELECT * FROM libraries LIMIT 10;
```

---

## 도메인 모델 (Entity)

### 1. User (사용자)
- 회원 정보 관리
- Spring Security의 `UserDetails` 구현
- **주요 필드**: 이메일, 비밀번호, 닉네임, 연령대, 성별, 프로필 이미지

### 2. Book (도서)
- 도서 기본 정보
- **주요 필드**: 제목, 저자, 출판사, ISBN, 도서 이미지, 출판일, 설명

### 3. BookRecord (독서 기록)
- 사용자의 독서 현황 관리 (내 서재)
- **주요 필드**: 독서 상태(읽는 중/완독/읽고 싶음), 시작일, 종료일, 후기, 평점
- **연관 관계**: User(N:1), Book(N:1)

### 4. Review (리뷰)
- 공개 리뷰 (다른 사용자도 볼 수 있음)
- **주요 필드**: 리뷰 내용, 리뷰 이미지, 평점
- **연관 관계**: User(N:1), Book(N:1)

### 5. Library (도서관)
- 전국 도서관 정보
- **주요 필드**: 정보나루 코드, 도서관명, 주소, 전화번호, 홈페이지, 위도, 경도

### 6. UserLibrary (내 도서관)
- 사용자가 즐겨찾기한 도서관
- **연관 관계**: User(N:1), Library(N:1)

### 7. RefreshToken
- JWT Refresh Token 관리
- **주요 필드**: 토큰 값, 사용자 이메일, 만료 시간

### 8. Enum 타입
- **ReadStatus**: WantToRead(읽고 싶음), Reading(읽는 중), Completed(완독)
- **AgeGroup**: TEENS(10대), TWENTIES(20대), THIRTIES(30대), FORTIES(40대), FIFTIES_PLUS(50대 이상)
- **Gender**: MALE(남성), FEMALE(여성), OTHER(기타)
- **RegionCode**: 지역 코드 (서울, 경기, 강원 등)
- **DetailedRegionCode**: 세부 지역 코드

---

## API 엔드포인트

### 인증 관련 (`/api/users`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | `/register` | 회원가입 | X |
| POST | `/login` | 로그인 (Access + Refresh Token 발급) | X |
| POST | `/logout` | 로그아웃 (Refresh Token 무효화) | O |
| POST | `/refresh` | Access Token 갱신 | X |
| GET | `/me` | 내 정보 조회 | O |
| PATCH | `/profile` | 프로필 수정 | O |
| GET | `/check-email` | 이메일 중복 확인 | X |
| GET | `/check-username` | 닉네임 중복 확인 | X |
| GET | `/{id}` | 특정 사용자 조회 | O |
| PUT | `/{id}` | 사용자 정보 수정 | O |
| DELETE | `/{id}` | 사용자 삭제 | O |

### 도서 관련 (`/api/books`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | `/search` | 도서 검색 (알라딘 API, 페이징) | X |
| GET | `/new-releases` | 신간 도서 목록 | X |
| GET | `/bestsellers` | 베스트셀러 목록 | X |
| GET | `/detail/{isbn}` | 도서 상세 정보 | X |
| POST | `/{isbn}` | ISBN으로 도서 저장 | X |
| POST | `/search-and-save` | 도서 검색 및 저장 | X |
| POST | `/search/libraries` | 도서 + 주변 도서관 검색 | X |

### 독서 기록 (`/api/records`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | `/` | 내 서재에 책 추가 | O |
| GET | `/my` | 내 서재 목록 조회 (상태별 필터링 가능) | O |
| GET | `/{recordId}` | 특정 기록 조회 | O |
| GET | `/book/{isbn}` | 특정 책에 대한 내 기록 조회 | O |
| PATCH | `/{recordId}` | 독서 상태 수정 | O |
| PUT | `/{recordId}/review` | 후기 및 평점 수정 | O |
| DELETE | `/{recordId}` | 서재에서 책 삭제 | O |

### 리뷰 (`/api/reviews`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | `/` | 리뷰 작성 | O |
| GET | `/books/{isbn}` | 특정 책의 모든 리뷰 조회 | X |
| GET | `/me` | 내가 작성한 모든 리뷰 조회 | O |
| GET | `/{reviewId}` | 특정 리뷰 조회 | X |
| PATCH | `/{reviewId}` | 리뷰 수정 (본인만) | O |
| DELETE | `/{reviewId}` | 리뷰 삭제 (본인만) | O |

### 도서관 (`/api/libraries`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | `/search` | 주변 도서관 검색 (위도/경도 기반) | X |
| GET | `/{d4lLibCode}/availability` | 특정 도서관의 도서 대출 가능 여부 | X |
| POST | `/{libraryId}/my-library` | 내 도서관에 추가 | O |
| GET | `/my-library` | 내 도서관 목록 조회 | O |
| DELETE | `/{libraryId}/my-library` | 내 도서관에서 제거 | O |
| GET | `/book-status` | 도서 소장 도서관 및 대출 가능 여부 | X |
| GET | `/all-from-api` | 정보나루 API - 전체 도서관 조회 (테스트용) | X |
| GET | `/search-from-api` | 정보나루 API - 지역별 도서관 검색 | X |

---

## 인증 및 보안

### JWT 기반 인증

1. **Access Token**: 1시간 유효
2. **Refresh Token**: 7일 유효, DB에 저장
3. **인증 헤더**: `Authorization: Bearer {token}`

### 로그인 플로우

```
1. POST /api/users/login
   → 이메일, 비밀번호 전송

2. 서버 응답:
   {
     "accessToken": "...",
     "refreshToken": "...",
     "userId": 1,
     "userNm": "홍길동",
     "userEmail": "user@example.com"
   }

3. 클라이언트:
   - accessToken을 메모리에 저장
   - refreshToken을 안전한 저장소(httpOnly 쿠키 권장)에 저장

4. API 요청 시:
   - Header: Authorization: Bearer {accessToken}

5. Access Token 만료 시:
   - POST /api/users/refresh
   - Body: { "refreshToken": "..." }
   - 새로운 Access Token + Refresh Token 발급
```

### CORS 설정

허용된 오리진:
- `http://localhost:5173` (Vite 개발 서버)
- `http://localhost:3000` (React 개발 서버)
- `http://localhost:4173` (Vite 프리뷰 서버)

---

## 외부 API 연동

### 1. 알라딘 API
- 도서 검색, 신간, 베스트셀러 정보 제공
- **엔드포인트**: `http://www.aladin.co.kr/ttb/api/`

### 2. 정보나루 API
- 도서관 정보, 도서 소장 정보 제공
- **엔드포인트**: `http://data4library.kr/api/`

### 3. 카카오 API
- (추후 사용 예정)

### 4. 국립중앙도서관 통합검색 (KolisNet)
- JSoup을 이용한 웹 크롤링
- 도서관별 도서 소장 및 대출 가능 여부 확인
- **서비스**: `KolisNetScraperService.java`

---

## 주요 서비스 로직

### UserService
- 회원가입, 로그인, 프로필 관리
- 비밀번호 암호화 (BCrypt)
- 중복 확인 (이메일, 닉네임)

### BookService
- 알라딘 API를 통한 도서 검색 및 저장
- 신간, 베스트셀러 목록 제공
- ISBN 기반 도서 정보 조회

### BookRecordService
- 사용자별 독서 기록 CRUD
- 독서 상태 관리 (읽고 싶음 → 읽는 중 → 완독)
- 독서 후기 및 평점 관리

### ReviewService
- 공개 리뷰 CRUD
- 책별 리뷰 목록 조회
- 본인 확인 (수정/삭제 권한)

### LibraryService
- 주변 도서관 검색 (거리 기반)
- 도서 소장 도서관 확인 (정보나루 API)
- 도서 대출 가능 여부 확인
- 내 도서관 즐겨찾기 관리

### LibraryBatchService
- 정보나루 API를 통한 전국 도서관 데이터 일괄 수집
- 페이징 처리 및 중복 방지
- 좌표 정보 검증

### KolisNetScraperService
- 국립중앙도서관 통합검색 크롤링
- 도서관별 도서 소장 및 대출 가능 여부 확인

### TokenService
- Refresh Token 생성, 저장, 검증, 삭제
- Token Rotation 지원

---

## 프로젝트 실행 방법

### 1. 환경 변수 설정 확인

```bash
# 필수 환경 변수
export DB_PASSWORD=데이터베이스_비밀번호
export JWT_SECRET=Base64로_인코딩된_비밀키
export ALADIN_API_KEY=알라딘_API_키
export DATA4LIBRARY_API_KEY=정보나루_API_키
export KAKAO_API_KEY=카카오_API_키
```

### 2. PostgreSQL 실행 확인

```bash
# WSL
sudo service postgresql start

# Mac (Homebrew)
brew services start postgresql

# Windows
# PostgreSQL 서비스가 실행 중인지 확인
```

### 3. 데이터베이스 생성 확인

```bash
sudo -u postgres psql
```

```sql
-- 데이터베이스 존재 확인
\l

-- 없으면 생성
CREATE DATABASE "BookProject";
CREATE USER bookproject WITH PASSWORD '비밀번호';
GRANT ALL PRIVILEGES ON DATABASE "BookProject" TO bookproject;
```

### 4. 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 또는 JAR 파일 실행
java -jar build/libs/BookProject-0.0.1-SNAPSHOT.jar
```

### 5. 애플리케이션 확인

```bash
# 헬스 체크 (별도 엔드포인트가 있다면)
curl http://localhost:8080/

# 회원가입 테스트
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "test@example.com",
    "userPw": "password123",
    "userNm": "테스터",
    "gender": "MALE",
    "ageGroup": "TWENTIES"
  }'
```

---

## 테스트

### 단위 테스트 실행

```bash
./gradlew test
```

### 주요 테스트 파일
- `UserServiceTest.java`: 사용자 서비스 테스트
- `BookRecordServiceTest.java`: 독서 기록 서비스 테스트
- `JwtTokenProviderTest.java`: JWT 토큰 생성/검증 테스트

---

## 트러블슈팅

### 1. 데이터베이스 연결 실패

**증상**: `Connection refused` 또는 `password authentication failed`

**해결 방법**:
1. PostgreSQL 서비스 실행 확인: `sudo service postgresql status`
2. 환경 변수 설정 확인: `echo $DB_PASSWORD`
3. 사용자 및 권한 확인:
   ```sql
   \du
   \l
   ```

### 2. JWT 토큰 검증 실패

**증상**: `잘못된 JWT 서명` 로그

**해결 방법**:
1. JWT_SECRET 환경 변수 확인
2. Base64로 인코딩된 충분히 긴 문자열인지 확인 (최소 256bit)

### 3. 도서관 데이터 수집 실패

**증상**: API 호출 오류 또는 빈 응답

**해결 방법**:
1. DATA4LIBRARY_API_KEY 확인
2. 정보나루 웹사이트에서 API 키 유효성 확인
3. 네트워크 연결 확인

### 4. CORS 오류

**증상**: 프론트엔드에서 `CORS policy` 오류

**해결 방법**:
1. `SecurityConfig.java`의 `allowedOrigins`에 프론트엔드 URL 추가
2. 프론트엔드 포트 확인 (기본: 5173)

---

## 디렉토리 구조

```
src/main/java/com/example/BookProject/
├── advice/                    # 전역 예외 처리
│   └── GlobalExceptionHandler.java
├── config/                    # 설정 파일
│   ├── RestTemplateConfig.java
│   └── SecurityConfig.java
├── controller/                # REST API 컨트롤러
│   ├── BookController.java
│   ├── BookRecordController.java
│   ├── LibraryController.java
│   ├── ReviewController.java
│   ├── UserController.java
│   └── UtilController.java
├── domain/                    # 엔티티 (도메인 모델)
│   ├── AgeGroup.java
│   ├── BaseEntity.java
│   ├── Book.java
│   ├── BookRecord.java
│   ├── DetailedRegionCode.java
│   ├── Gender.java
│   ├── Library.java
│   ├── ReadStatus.java
│   ├── RefreshToken.java
│   ├── RegionCode.java
│   ├── Review.java
│   ├── User.java
│   └── UserLibrary.java
├── dto/                       # 데이터 전송 객체
│   ├── AladinDto.java
│   ├── ApiResponse.java
│   ├── BookDto.java
│   ├── BookRecordCreateRequestDto.java
│   ├── BookRecordResponseDto.java
│   ├── BookRecordUpdateRequestDto.java
│   ├── KakaoApiResponseDto.java
│   ├── LibraryBookDto.java
│   ├── LibraryBookStatusDto.java
│   ├── LibraryDto.java
│   ├── ReviewRequestDto.java
│   ├── ReviewResponseDto.java
│   ├── ReviewUpdateRequestDto.java
│   └── UserDto.java
├── jwt/                       # JWT 인증
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── repository/                # 데이터 접근 계층
│   ├── BookRecordRepository.java
│   ├── BookRepository.java
│   ├── LibraryRepository.java
│   ├── RefreshTokenRepository.java
│   ├── ReviewRepository.java
│   ├── UserLibraryRepository.java
│   └── UserRepository.java
├── service/                   # 비즈니스 로직
│   ├── BookRecordService.java
│   ├── BookSearchService.java
│   ├── BookService.java
│   ├── CustomUserDetailsService.java
│   ├── KakaoApiService.java
│   ├── KolisNetScraperService.java
│   ├── LibraryBatchService.java
│   ├── LibraryService.java
│   ├── ReviewService.java
│   ├── TokenService.java
│   ├── UserService.java
│   └── UtilService.java
├── BatchTestRunner.java       # 배치 테스트 러너
├── ExcelDataProcessor.java   # Excel 데이터 처리
└── BookProjectApplication.java # 메인 애플리케이션

src/main/resources/
└── application.yml            # 애플리케이션 설정

src/test/                      # 테스트 코드
└── ...
```

---

## 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Security 공식 문서](https://spring.io/projects/spring-security)
- [JWT 소개](https://jwt.io/)
- [정보나루 API 문서](https://www.data4library.kr/api)
- [알라딘 API 문서](http://blog.aladin.co.kr/ttb/category/4502)

---

## 라이선스

이 프로젝트는 교육 목적의 졸업 프로젝트입니다.

---

## 작성자

- **프로젝트명**: BookProject (독서 기록 및 도서관 정보 서비스)
- **작성일**: 2025년
- **백엔드 기술**: Spring Boot + PostgreSQL + JWT
