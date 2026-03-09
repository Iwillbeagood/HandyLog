---
description: "새 기능을 처음부터 끝까지 완전 구현 - Data Layer → Domain Layer → Feature Layer → Navigation 연결까지 전체 레이어 순차 생성"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# 전체 기능 구현 Skill

$ARGUMENTS 를 기반으로 하나의 기능을 Data Layer부터 Navigation 연결까지 완전히 구현합니다. 서버 Response 구조를 먼저 파악한 뒤 Domain Model을 설계합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `CargoReturn`, `Notice`, `Block`)
- **비즈니스 요구사항**: 데이터 구조, 비즈니스 로직
- **API 스펙**: 엔드포인트, Request/Response (API가 있는 경우)
- **로컬 저장**: Room 또는 DataStore 필요 여부
- **UI 요구사항**: 화면 구성, 사용자 인터랙션

인자가 불충분한 경우 사용자에게 질문하여 확인합니다.

## 구현 플로우

```
Phase 1: Data Layer (Remote / Local) — DTO, API, Mapper 먼저 구현
    ↓
Phase 2: Domain Layer — Response 구조를 바탕으로 Model 설계
    ↓
Phase 3: Feature (Presentation) Layer
    ↓
Phase 4: Navigation 연결
    ↓
Phase 5: 검증
```

---

## Phase 1: Data Layer

`.github/prompts/data-layer-prompt.md`를 참조합니다.

### A. Remote API가 있는 경우 (9단계)

| 단계 | 모듈 | 파일 |
|------|------|------|
| 1 | `:remote:model` | DTO (Request/Response) - `BaseResponse`/`BaseRequest` 상속 |
| 2 | `:remote:api` | API Interface - `ApiResult<T>` 반환 |
| 3 | `:remote:mapper` | Mapper - `{Response}.to{Domain}()` |
| 4 | `:data:datasource` | RemoteDataSource IF - `Flow<DomainModel>` 반환 |
| 5 | `:remote:datasource` | RemoteDataSource Impl - `flow { }` + `suspendOnSuccess` |
| 6 | `:remote:network` | ApiModule - `@Provides` 등록 |
| 7 | `:remote:network` | RemoteDataSourceModule - `@Binds` 등록 |
| 8 | `:data:repositoryImpl` | RepositoryImpl - DataSource 위임 |
| 9 | `:data:repositoryImpl` | RepositoryModule - `@Binds` 등록 |

### B. Local 저장이 필요한 경우

**Room** (7단계):
Entity → DAO → TypeConverter → Mapper → DataSource IF → DataSource Impl → Hilt (DaoModule + LocalDataSourceModule) + CarManRoomDatabase 등록

**DataStore** (5단계):
Prefs 모델 → DataSource IF → DataSource Impl → Hilt (DataStoreModule + LocalDataSourceModule)

### 핵심 규칙
- 패키지 `datasoure` 오타 유지 (`data/datasource/src/.../data/datasoure/`)
- Response는 `BaseResponse()` 상속
- Request는 `BaseRequest()` 상속
- RepositoryImpl은 `internal class`
- RemoteDataSourceImpl의 에러: `suspendOnFailureWithErrorHandling(onError)` 사용

---

## Phase 2: Domain Layer

`.github/prompts/domain-layer-prompt.md`를 참조합니다.

Phase 1에서 정의한 Response 구조를 바탕으로 Domain Model을 설계합니다.

### 구현 항목

1. **Domain Model** (`domain/model/src/main/kotlin/ktc/cargo/driver/domain/model/{feature}/`)
   - `data class` 또는 `sealed interface`
   - 순수 Kotlin, Android 의존성 없음
   - Response 필드 중 UI에 필요한 것만 추출하여 정의

2. **Repository Interface** (`domain/repository/src/main/kotlin/ktc/cargo/driver/domain/repository/`)
   - `Flow<T>` (조회) 또는 `suspend fun` (일회성)
   - `onError: (MessageType) -> Unit` 콜백

3. **UseCase** (`domain/usecase/src/main/kotlin/ktc/cargo/driver/domain/usecase/{feature}/`)
   - `@Inject constructor`, `operator fun invoke()`
   - Validation → Repository 조합

### 핵심 규칙
- Domain → Data 의존 절대 금지
- DTO, Entity 사용 금지
- 순수 Kotlin + Kotlinx Coroutines만 허용

### Phase 1 → Phase 2 연결

Domain Model 정의 후, Phase 1에서 만든 Mapper와 DataSource를 완성합니다:
- Mapper: `{Response}.to{DomainModel}()` 구현
- RemoteDataSource: Domain Model을 반환하도록 연결
- RepositoryImpl: DataSource를 위임하여 Repository Interface 구현

---

## Phase 3: Feature (Presentation) Layer

### 모듈 생성

`feature/{feature-name}/build.gradle.kts`:
```kotlin
import ktc.cargo.driver.setNamespace

plugins {
	alias(libs.plugins.hmm.android.feature)
}

android {
	setNamespace("{featureName}")
}

dependencies {
	implementation(projects.domain.usecase)
}
```

### 구현 항목 (6단계)

| 단계 | 파일 | 위치 |
|------|------|------|
| 1 | State | `contract/{Name}State.kt` - `@Stable sealed interface` |
| 2 | Effect | `contract/{Name}Effect.kt` - ModalEffect + Effect |
| 3 | ViewModel | `{Name}ViewModel.kt` - `@HiltViewModel internal class` |
| 4 | Route | `{Name}Route.kt` - State 수집 + Effect 핸들링 |
| 5 | Screen | `{Name}Screen.kt` - 실제 UI |
| 6 | Navigation | `navigation/{Name}Navigation.kt` - NavController/NavGraphBuilder 확장 |

### 핵심 규칙
- **ViewModel 함수**: `on` 접두어 없음 (예: `selectTab`, `search`)
- **Composable 콜백**: `on` 접두어 사용 (예: `onTabSelected`, `onSearchClick`)
- **상태 타입 체크**: `withData<T>` / `updateWithData<State, T>` 유틸 사용
- **MutableStateFlow 상태 변경**: `.update { }` 사용 (`.value =` 금지)
- **Flow 수집**: `viewModelScope.launch { flow.collect { } }` (launchIn 금지)
- **색상**: `KTCTheme.colorScheme` 사용 (`MaterialTheme.colorScheme` 금지)
- **타이포**: `KTCTheme.typography` 사용
- **Preview**: `@ThemePreviews` + `HmmTheme { }` 감싸기

---

## Phase 4: Navigation 연결

5곳을 수정합니다:

| 순서 | 파일 | 작업 |
|------|------|------|
| 1 | `core/navigation/.../Route.kt` | `@Serializable` Route 추가 |
| 2 | `core/navigation/.../NavigateActionInterop.kt` | `navigateTo{Feature}()` 메서드 추가 |
| 3 | `feature/main/.../MainNavigator.kt` | override 구현 + import 추가 |
| 4 | `feature/main/.../MainNavHost.kt` | `{feature}NavGraph()` 호출 + import 추가 |
| 5 | `feature/main/build.gradle.kts` | `implementation(projects.feature.{name})` 추가 |

추가로 `settings.gradle.kts`에 새 모듈 등록 확인.

---

## Phase 5: 검증

구현 완료 후 다음을 확인합니다:

### Domain Layer
- [ ] Domain Model이 순수 Kotlin인지
- [ ] Repository Interface가 Domain Model만 사용하는지
- [ ] UseCase에 Validation이 포함되어 있는지

### Data Layer
- [ ] Response가 `BaseResponse()` 상속하는지
- [ ] API 반환 타입이 `ApiResult<T>`인지
- [ ] Hilt 모듈 3곳 (ApiModule, RemoteDataSourceModule, RepositoryModule) 등록했는지
- [ ] Local 사용 시 Hilt 모듈 등록했는지

### Feature Layer
- [ ] ViewModel이 `@HiltViewModel internal class`인지
- [ ] `withData`/`updateWithData` 사용하는지
- [ ] MutableStateFlow 상태 변경에 `.update { }` 사용하는지 (`.value =` 금지)
- [ ] `KTCTheme.colorScheme` 사용하는지 (`MaterialTheme` 금지)
- [ ] `KTCTheme.typography` 사용하는지

### Navigation
- [ ] Route.kt, NavigateActionInterop, MainNavigator, MainNavHost, build.gradle.kts 5곳 모두 수정했는지
- [ ] settings.gradle.kts에 모듈 등록되어 있는지

---

## 참조 문서

- **Domain Layer 상세**: `.github/prompts/domain-layer-prompt.md`
- **Data Layer 상세**: `.github/prompts/data-layer-prompt.md`
- **디자인 시스템**: `.github/prompts/design-system.prompt.md`
- **Feature Template**: `featureTemplate/feature/` (보일러플레이트 참고)
