# 🎯 프로젝트 완성 로드맵 (100% 문서화)

## 📋 담당 기능
- 재료/소스 관리 (등록, 삭제)
- 오늘의 식단 (당일 인기글)
- 명예의 식단 (주간 인기글)

## 🏗️ 개발 전략: 조회 우선

### Phase 1: 기본 조회 기능 구현

#### 커밋 1: 재료 목록 조회 Repository
```bash
git checkout -b feature/ingredient-management
git add src/main/java/.../IngredientRepository.java
git commit -m "feat: 사용자 재료 목록 조회 Repository 추가"
```
- `findByUserId()` 메서드 구현
- `user_ingredients` 테이블 조회 SQL 작성
- Soft Delete 조건 추가 (`deleted_at IS NULL`)

#### 커밋 2: 재료 목록 조회 Service
```bash
git add src/main/java/.../IngredientService.java
git commit -m "feat: 재료 목록 조회 Service 로직 구현"
```
- `getMyIngredients()` 비즈니스 로직
- DTO 변환 로직
- 예외 처리 추가

#### 커밋 3: 재료 목록 조회 Controller
```bash
git add src/main/java/.../IngredientController.java
git commit -m "feat: 재료 목록 조회 API 엔드포인트 구현"
```
- `GET /api/ingredients` 엔드포인트
- Spring Security 인증 연동
- ApiResponse 형식으로 응답

### Phase 2: 랭킹 조회 기능 구현

#### 커밋 4: 랭킹 조회 Repository
```bash
git add src/main/java/.../RankingRepository.java
git commit -m "feat: 랭킹 조회 Repository 및 SQL 추가"
```
- `selectTodayBestRecipes()` SQL 구현
- `selectWeeklyBestRecipes()` SQL 구현
- 점수 계산공식: `(좋아요 × 3) + (댓글 × 2) + (조회수 × 0.2)`

#### 커밋 5: 랭킹 DTO 설계
```bash
git add src/main/java/.../RankingDto.java
git commit -m "feat: 랭킹 응답 DTO 설계 및 구현"
```
- `RecipeRankingDto` 필드 정의
- 난이도, 시간 포맷팅 로직
- Builder 패턴 적용

#### 커밋 6: 랭킹 조회 Service
```bash
git add src/main/java/.../RankingService.java
git commit -m "feat: 랭킹 조회 Service 비즈니스 로직 구현"
```
- `getTodayRanking()` 로직
- `getWeeklyRanking()` 로직
- 실시간 점수 계산

#### 커밋 7: 랭킹 조회 Controller
```bash
git add src/main/java/.../RankingController.java
git commit -m "feat: 오늘의 식단 및 명예의 식단 API 구현"
```
- `GET /api/ranking/today` 엔드포인트
- `GET /api/ranking/weekly` 엔드포인트
- 빈 리스트 처리 로직

### Phase 3: 재료 CUD 기능 구현

#### 커밋 8: 재료 등록 Repository
```bash
git add src/main/java/.../IngredientRepository.java
git commit -m "feat: 재료 등록 Repository 로직 추가"
```
- `addUserIngredient()` 메서드
- `ON DUPLICATE KEY UPDATE` 로직
- 정규화된 이름 저장

#### 커밋 9: 재료 등록 Service
```bash
git add src/main/java/.../IngredientService.java
git commit -m "feat: 재료 등록 3단계 정규화 로직 구현"
```
- 전처리 → Alias 조회 → AI 정규화
- 중복 재료 처리
- 예외 처리 및 로깅

#### 커밋 10: 재료 등록 Controller
```bash
git add src/main/java/.../IngredientController.java
git commit -m "feat: 재료 등록 API 엔드포인트 구현"
```
- `POST /api/ingredients` 엔드포인트
- `@Valid` 검증 로직
- 성공/실패 응답 처리

#### 커밋 11: 재료 삭제 기능
```bash
git add src/main/java/.../IngredientController.java src/main/java/.../IngredientService.java
git commit -m "feat: 재료 Soft Delete 기능 구현"
```
- `DELETE /api/ingredients/{id}` 엔드포인트
- Soft Delete 로직 (`deleted_at = NOW()`)
- 권한 확인 로직

### Phase 4: 최종 테스트 및 리팩토링

#### 커밋 12: 공통 응답 포맷
```bash
git add src/main/java/.../common/ApiResponse.java
git commit -m "refactor: 공통 ApiResponse 클래스 도입"
```

#### 커밋 13: 예외 처리 개선
```bash
git add src/main/java/.../exception/
git commit -m "feat: 커스텀 예외 클래스 및 글로벌 핸들러 추가"
```

#### 커밋 14: 테스트 코드
```bash
git add src/test/java/.../IngredientControllerTest.java
git commit -m "test: 재료 관리 API 통합 테스트 추가"
```

## 🎯 PR 생성 순서

### PR 1: 재료 관리 기본 조회
```
[본인이름] 재료 관리 기본 조회 기능 구현

- 내 재료 목록 조회 API
- Repository 및 Service 로직
- Spring Security 인증 연동
```

### PR 2: 랭킹 시스템 조회
```
[본인이름] 오늘의 식단 및 명예의 식단 조회 기능

- 일간/주간 인기 레시피 조회 API
- 실시간 점수 계산 로직
- 랭킹 DTO 설계
```

### PR 3: 재료 관리 CUD
```
[본인이름] 재료 등록 및 삭제 기능 구현

- 재료 등록 API (3단계 정규화)
- Soft Delete 삭제 기능
- 중복 재료 처리 로직
```

## 📊 완성도 체크리스트

| 단계 | 기능 | 커밋 수 | 예상 시간 | 상태 |
|------|------|---------|----------|------|
| Phase 1 | 조회 기능 | 7개 | 2시간 | ⏳ |
| Phase 2 | CUD 기능 | 4개 | 1시간 | ⏳ |
| Phase 3 | 리팩토링 | 3개 | 30분 | ⏳ |

**총 14개 커밋으로 담당 기능 100% 완성 예정**

---

## 📝 참고 자료

### 데모프로젝트 참고할 부분
- FridgeApiController → IngredientController (95% 활용 가능)
- RecipeRepository 랭킹 쿼리 (60% 활용 가능)

### 요구사항 문서 기반
- shudocode.md: 상세 로직 흐름
- exampledetailcode.md: 구체적인 조건 명시
