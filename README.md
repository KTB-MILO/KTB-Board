# KTB Board

Spring Boot 기반 게시판 REST API 서버. Spring Security 없이 세션 쿠키로 인증/인가를 직접 구현했다.

## 기술 스택

- Java 25 / Spring Boot 4.0.6
- Spring Data JPA / Hibernate 7
- MySQL 8.4 (Docker)
- Lombok

---

## 패키지 구조

```
com.KTB.Board
├── auth/        로그인·로그아웃
├── user/        회원 CRUD
├── post/        게시글 CRUD + 좋아요
├── comment/     댓글 CRUD
├── session/     세션 엔티티·레포지토리
└── common/      공통 (응답·예외·인터셉터·유틸)
```

계층(controller/service/repository) 기준이 아닌 **도메인 기준**으로 패키지를 나눴다.  
기능이 추가될 때 하나의 도메인 폴더 안에서만 작업이 끝나고, 관련 코드를 찾기 위해 여러 패키지를 오갈 필요가 없다.

---

## 인증·인가 설계

### Spring Security를 쓰지 않은 이유

Spring Security를 사용하면 FilterChain, SecurityContext, AuthenticationProvider 등 많은 개념을 이해해야 한다.  
이 프로젝트는 인증이 어떻게 동작하는지를 직접 구현하는 것이 목적이므로, 세션 테이블 + 인터셉터 방식으로 직접 만들었다.

### 전체 흐름

```
[로그인]
클라이언트 → POST /auth → AuthService.login()
  → DB에서 이메일 조회 → 비밀번호 해시 비교
  → UUID 세션 생성 → user_sessions 테이블에 저장
  → 응답 헤더에 Set-Cookie: SESSION_ID={uuid}

[인증이 필요한 요청]
클라이언트 → Cookie: SESSION_ID={uuid} → AuthInterceptor.preHandle()
  → 쿠키에서 세션 ID 추출 → user_sessions 테이블 조회
  → 만료 여부 확인 → request.setAttribute("userId", ...) → 컨트롤러 진입

[로그아웃]
클라이언트 → DELETE /auth → AuthService.logout()
  → user_sessions에서 세션 행 삭제
  → 응답에 MaxAge=0 쿠키 → 브라우저 쿠키 삭제
```

---

### `@RequireAuth` 어노테이션

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {}
```

인증이 필요한 메서드에만 붙이는 마커 어노테이션이다.  
경로(URL) 기반으로 인증 여부를 설정하면 엔드포인트가 추가될 때마다 설정 파일을 수정해야 한다.  
어노테이션 방식은 메서드 선언부만 보면 인증 필요 여부를 바로 알 수 있고, 설정이 코드와 함께 위치해 누락되기 어렵다.

`@Retention(RetentionPolicy.RUNTIME)`이 필요한 이유: 인터셉터는 런타임에 리플렉션으로 어노테이션을 읽기 때문이다.

---

### `AuthInterceptor`

```java
@Override
public boolean preHandle(...) {
    if (!(handler instanceof HandlerMethod method)) return true;  // (1)
    if (!method.hasMethodAnnotation(RequireAuth.class)) return true;  // (2)

    String sessionId = extractSessionId(request);  // (3)
    UserSession session = sessionRepository.findById(sessionId).orElse(null);
    if (session == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {  // (4)
        sendUnauthorized(response);
        return false;
    }
    request.setAttribute("userId", session.getUser().getId());  // (5)
    return true;
}
```

1. `handler`가 `HandlerMethod`가 아닌 경우(정적 리소스 요청 등)는 바로 통과
2. `@RequireAuth`가 없는 메서드는 검사 없이 통과 → 인증이 필요 없는 엔드포인트에 영향 없음
3. 쿠키 배열에서 `SESSION_ID` 이름의 쿠키 값을 추출
4. 세션이 없거나 만료됐으면 401 반환. `return false`로 컨트롤러 진입 자체를 차단
5. 검증된 `userId`를 request attribute에 저장 → 컨트롤러에서 `(Long) request.getAttribute("userId")`로 꺼냄

`preHandle()`이 `false`를 반환하면 이후의 인터셉터와 컨트롤러가 실행되지 않는다.

**`UserSession`의 User를 `FetchType.EAGER`로 설정한 이유**  
인터셉터는 트랜잭션 밖에서 실행된다. `sessionRepository.findById()` 호출 후 트랜잭션이 닫히기 때문에, LAZY로 설정하면 `session.getUser().getId()` 호출 시 `LazyInitializationException`이 발생할 수 있다.  
세션 조회 시에는 항상 사용자 정보가 필요하므로 EAGER로 설정해 한 번의 JOIN 쿼리로 같이 조회한다.

---

### `PasswordUtil`

```java
// 저장 형식: {Base64(salt)}:{Base64(SHA-256(salt + password))}
public static String hash(String rawPassword) {
    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);  // (1)
    byte[] hash = digest(salt, rawPassword);
    return Base64.getEncoder().encodeToString(salt) + ":" + ...
}

public static boolean matches(String rawPassword, String storedPassword) {
    String[] parts = storedPassword.split(":");
    byte[] salt = Base64.getDecoder().decode(parts[0]);  // (2)
    byte[] expected = Base64.getDecoder().decode(parts[1]);
    byte[] actual = digest(salt, rawPassword);
    return MessageDigest.isEqual(expected, actual);  // (3)
}
```

1. `SecureRandom`으로 16바이트 무작위 salt 생성. 같은 비밀번호라도 저장값이 매번 달라져 레인보우 테이블 공격을 방어한다
2. 검증 시에는 저장된 salt를 꺼내 동일한 방식으로 해시 → salt가 비밀번호와 함께 저장되어 있어 검증 가능
3. `MessageDigest.isEqual()`은 두 배열의 길이가 달라도 일정한 시간이 걸리는 **상수 시간 비교**를 수행한다. 일반 `Arrays.equals()`는 첫 번째 불일치 지점에서 바로 반환되어 타이밍 공격에 취약하다

Spring Security 없이 구현하므로 BCrypt 대신 Java 표준 라이브러리(MessageDigest)만 사용했다.

---

## 엔티티 설계

### `likeCount`, `commentCount`를 Post에 직접 보관

```java
// Post.java
private int viewCount = 0;
private int likeCount = 0;
private int commentCount = 0;
```

게시글 목록을 조회할 때마다 `COUNT(*)` 집계 쿼리를 실행하는 대신, 댓글/좋아요 추가·삭제 시점에 카운트를 직접 증감한다.  
목록 조회는 빈번하고, 카운트 변경은 상대적으로 드물기 때문에 읽기 성능을 우선했다.

### 연관관계 Fetch 전략

```java
// Post → User: LAZY
@ManyToOne(fetch = FetchType.LAZY)
private User user;

// Post → Comment, PostLike: LAZY (기본값)
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Comment> comments = new ArrayList<>();
```

`@ManyToOne`과 `@OneToMany` 모두 필요할 때만 조회하도록 LAZY를 기본으로 사용한다.  
EAGER를 쓰면 필요 없는 경우에도 항상 JOIN이 발생해 성능이 떨어진다.  
예외적으로 `UserSession → User`만 EAGER를 사용하는데, 인터셉터(트랜잭션 밖)에서 User 정보가 항상 필요하기 때문이다.

### `cascade = CascadeType.ALL, orphanRemoval = true`

```java
// Post.java
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Comment> comments = new ArrayList<>();
```

게시글을 삭제하면 달린 댓글과 좋아요도 함께 삭제된다.  
`CascadeType.ALL`: 부모(Post)에 대한 영속성 작업을 자식(Comment, PostLike)에게도 전파  
`orphanRemoval = true`: 컬렉션에서 제거된 자식 엔티티를 자동으로 DELETE

### `@Builder.Default`

```java
@Builder.Default
private int viewCount = 0;

@Builder.Default
private List<Comment> comments = new ArrayList<>();
```

`@Builder`를 사용하면 필드의 기본값이 무시된다. `@Builder.Default`를 붙여야 빌더로 객체를 생성할 때도 초기값이 적용된다.  
이것이 없으면 `Post.builder().build()` 시 `viewCount`는 0이지만 `comments`는 `null`이 되어 NPE가 발생한다.

### `PostLike`의 복합 유니크 제약

```java
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
```

한 사용자가 같은 게시글에 좋아요를 중복으로 누르지 못하도록 DB 레벨에서 강제한다.  
애플리케이션 레벨 검사만으로는 동시 요청이 왔을 때 두 요청이 모두 통과할 수 있다(레이스 컨디션).  
DB 유니크 제약은 이를 원천 차단한다.

---

## 공통 응답·예외 처리

### `ApiResponse<T>`

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final String message;
    private final T data;
}
```

모든 응답을 `{ "message": "...", "data": {...} }` 형태로 통일한다.  
`@JsonInclude(NON_NULL)`: data가 null이면 JSON에서 data 필드 자체를 생략한다. 성공 메시지만 반환할 때 `"data": null`이 노출되지 않는다.  
생성자를 `private`으로 막고 `of()` 정적 팩토리 메서드만 공개해 잘못된 생성을 방지했다.

### `ErrorCode` enum

```java
public enum ErrorCode {
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", 401),
    BOARD_NOT_FOUND("BOARD_NOT_FOUND", 404),
    ...
}
```

에러 코드 문자열과 HTTP 상태 코드를 한 곳에서 관리한다.  
`GlobalExceptionHandler`에서 `ErrorCode`를 받아 상태 코드와 메시지를 함께 꺼내므로, 에러를 추가할 때 enum 한 곳만 수정하면 된다.

### `GlobalExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)     // 비즈니스 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)  // @Valid 실패
    @ExceptionHandler(Exception.class)           // 그 외 모든 예외
}
```

예외를 컨트롤러마다 처리하지 않고 한 곳에서 처리한다.  
`@ExceptionHandler(Exception.class)`가 있어 예상치 못한 예외도 `INTERNAL_SERVER_ERROR`로 응답하고 500이 그대로 노출되지 않는다.  
`CustomException` → `MethodArgumentNotValidException` → `Exception` 순으로 구체적인 타입이 먼저 매칭된다.

---

## 레포지토리 설계

### `@Modifying @Query`를 쓴 이유

```java
// CommentRepository.java
@Modifying
@Query("DELETE FROM Comment c WHERE c.post.user.id = :userId")
void deleteByPostUserId(@Param("userId") Long userId);
```

Spring Data의 파생 삭제 메서드(`deleteBy...`)는 내부적으로 대상 엔티티를 먼저 전부 조회한 뒤 하나씩 삭제한다.  
데이터가 많으면 N번의 DELETE 쿼리가 발생한다.  
`@Modifying @Query`는 JPQL로 단일 DELETE 쿼리를 직접 실행하므로 대량 삭제에 효율적이다.

### 회원 탈퇴 시 삭제 순서

```java
// UserService.deleteUser()
sessionRepository.deleteByUserId(userId);       // 1. 세션
postLikeRepository.deleteByPostUserId(userId);  // 2. 내 게시글의 좋아요 (타인 것 포함)
commentRepository.deleteByPostUserId(userId);   // 3. 내 게시글의 댓글 (타인 것 포함)
postLikeRepository.deleteByUserId(userId);      // 4. 내가 누른 좋아요 (타인 게시글)
commentRepository.deleteByUserId(userId);       // 5. 내가 쓴 댓글 (타인 게시글)
postRepository.deleteByUserId(userId);          // 6. 내 게시글
userRepository.deleteById(userId);              // 7. 유저
```

외래 키(FK) 제약 때문에 참조하는 쪽을 먼저 삭제해야 한다.  
예를 들어 게시글(posts)을 먼저 삭제하면 해당 게시글을 참조하는 댓글·좋아요 행이 남아 FK violation이 발생한다.  
자식 테이블부터 순서대로 삭제한 뒤 부모 테이블을 삭제한다.

---

## 서비스 계층 설계

### `@Transactional(readOnly = true)`

```java
// PostService.java
@Transactional(readOnly = true)
public PageResponse<PostListResponse> getPosts(int page) { ... }
```

조회 전용 메서드에 `readOnly = true`를 설정하면 Hibernate의 dirty checking(변경 감지)을 생략한다.  
트랜잭션 커밋 시 스냅샷 비교를 하지 않아 성능이 향상되고, 실수로 데이터를 변경하는 것을 방지한다.

### Dirty Checking으로 UPDATE 생략

```java
// PostService.updatePost()
@Transactional
public void updatePost(...) {
    Post post = findPost(postId);
    if (request.getTitle() != null) post.setTitle(request.getTitle());
    // postRepository.save(post) 호출 없음
}
```

`@Transactional` 안에서 조회한 엔티티는 영속 상태(Managed)다.  
트랜잭션이 커밋될 때 Hibernate가 최초 조회 시점의 스냅샷과 현재 상태를 비교해 변경된 필드만 자동으로 UPDATE한다.  
`save()`를 명시적으로 호출할 필요가 없다.

### 좋아요 토글 `ifPresentOrElse()`

```java
// PostService.toggleLike()
postLikeRepository.findByPostAndUser(post, user).ifPresentOrElse(
    like -> {
        postLikeRepository.delete(like);
        post.setLikeCount(post.getLikeCount() - 1);  // 이미 눌렀으면 취소
    },
    () -> {
        postLikeRepository.save(...);
        post.setLikeCount(post.getLikeCount() + 1);  // 안 눌렀으면 등록
    }
);
```

좋아요 존재 여부를 조회한 뒤 결과에 따라 분기한다.  
`Optional.ifPresentOrElse()`를 사용해 if/else 없이 두 경우를 명확하게 표현했다.  
`@Transactional` 안에서 `post.setLikeCount()`를 호출하면 dirty checking으로 Post도 자동 UPDATE된다.

---

## DB 설정

### `ddl-auto=update`

```properties
spring.jpa.hibernate.ddl-auto=update
```

`create`: 실행마다 테이블을 새로 생성 → 데이터 유실  
`create-drop`: 종료 시 테이블 삭제 → 데이터 유실  
`update`: 엔티티 변경 사항만 반영, 기존 데이터 유지  
`validate`: 스키마 일치 여부만 검사  

개발 환경에서는 `update`를 사용해 엔티티를 수정해도 데이터가 유지되게 했다.  
운영 환경에서는 `validate`로 바꾸고 스키마 변경은 마이그레이션 도구(Flyway 등)로 관리하는 것이 안전하다.

### Docker Compose

```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
  interval: 10s
  retries: 5
```

MySQL 컨테이너는 프로세스가 시작돼도 실제로 연결을 받을 준비가 되기까지 시간이 걸린다.  
healthcheck를 설정하면 `healthy` 상태가 된 후에 앱이 연결을 시도하도록 조율할 수 있다.

```yaml
volumes:
  mysql-data:
```

컨테이너를 내려도 데이터가 호스트의 named volume에 남아 있어 재시작 후에도 데이터가 유지된다.

---

## API 명세

| 메서드 | 경로 | 인증 | 설명 |
|--------|------|------|------|
| POST | /auth | X | 로그인 |
| DELETE | /auth | O | 로그아웃 |
| POST | /users | X | 회원가입 |
| GET | /users/{userId} | X | 회원 조회 |
| PATCH | /users/{userId} | O | 회원 정보 수정 |
| PUT | /users/password | O | 비밀번호 변경 |
| DELETE | /users/{userId} | O | 회원 탈퇴 |
| GET | /posts?page={n} | X | 게시글 목록 (10개씩) |
| GET | /posts/{postId} | X | 게시글 상세 |
| POST | /posts | O | 게시글 작성 |
| PATCH | /posts/{postId} | O | 게시글 수정 |
| DELETE | /posts/{postId} | O | 게시글 삭제 |
| PUT | /posts/{postId}/likes | O | 좋아요 등록/취소 |
| POST | /posts/{postId}/comments | O | 댓글 작성 |
| PUT | /comments/{commentId} | O | 댓글 수정 |
| DELETE | /comments/{commentId} | O | 댓글 삭제 |

## 실행 방법

```bash
# MySQL 컨테이너 시작
docker compose up -d

# 앱 실행
./gradlew bootRun
```
