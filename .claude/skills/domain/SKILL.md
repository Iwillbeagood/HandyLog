---
description: "Domain Layer 구현 - 순수 Kotlin Model 정의, Repository 인터페이스, UseCase 비즈니스 로직 작성"
model: claude-sonnet-4-5-20250929
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Domain Layer Skill

$ARGUMENTS 를 기반으로 Domain Layer를 구현합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `CargoReturn`, `Notice`, `Block`)
- **데이터 구조**: 필요한 필드 및 비즈니스 요구사항

## 구현 순서

### 1단계: Domain Model 정의

**위치**: `domain/model/src/main/kotlin/ktc/cargo/driver/domain/model/{feature}/`

**규칙**:
- 순수 Kotlin (Android 의존성 절대 금지)
- `data class` 또는 `sealed interface` 사용
- 불변 객체 (`val` 필드만)
- 패키지: `ktc.cargo.driver.domain.model.{feature}`

**data class 사용 시기**: 데이터를 담는 모델, 값 객체, API 응답 매핑 결과
**sealed interface 사용 시기**: 제한된 타입 계층, 상태/이벤트/에러 타입 표현

**참고 패턴**:
```kotlin
// data class 예시
package ktc.cargo.driver.domain.model.login

data class LoginData(
	val userId: String,
	val userName: String,
	val userSequence: Int,
	val token: String,
)
```

```kotlin
// sealed interface 예시
package ktc.cargo.driver.domain.model

sealed interface ConnectionState {
	data object Available : ConnectionState
	data object Unavailable : ConnectionState
}
```

**참고 파일** (패턴 확인용):
- `domain/model/src/main/kotlin/ktc/cargo/driver/domain/model/login/LoginData.kt`
- `domain/model/src/main/kotlin/ktc/cargo/driver/domain/model/block/BlockedCorp.kt`
- `domain/model/src/main/kotlin/ktc/cargo/driver/domain/model/MessageType.kt`

### 2단계: Repository Interface 정의

**위치**: `domain/repository/src/main/kotlin/ktc/cargo/driver/domain/repository/`

**규칙**:
- `interface`로 정의 (구현은 Data Layer에서)
- 패키지: `ktc.cargo.driver.domain.repository`
- 도메인 모델만 사용 (DTO, Entity 사용 금지)
- 에러 콜백: `onError: (MessageType) -> Unit`

**Flow vs suspend fun 선택**:
- `Flow<T>`: 데이터 조회, 실시간 스트림, 여러 값 방출
- `suspend fun`: 단일 결과, 일회성 CRUD 작업

**참고 패턴**:
```kotlin
package ktc.cargo.driver.domain.repository

import kotlinx.coroutines.flow.Flow
import ktc.cargo.driver.domain.model.MessageType
import ktc.cargo.driver.domain.model.block.BlockedCorp

interface BlockRepository {

	fun getBlockedList(
		limit: Int = 100,
		offset: Int = 0,
		onError: (MessageType) -> Unit,
	): Flow<List<BlockedCorp>>

	suspend fun updateBlockStatus(
		corpSeq: Int,
		isBlocked: Boolean,
		onSuccess: () -> Unit,
		onError: (MessageType) -> Unit,
	)
}
```

**참고 파일**:
- `domain/repository/src/main/kotlin/ktc/cargo/driver/domain/repository/LoginRepository.kt`
- `domain/repository/src/main/kotlin/ktc/cargo/driver/domain/repository/BlockRepository.kt`
- `domain/repository/src/main/kotlin/ktc/cargo/driver/domain/repository/TokenRepository.kt`
- `domain/repository/src/main/kotlin/ktc/cargo/driver/domain/repository/SettingRepository.kt`

### 3단계: UseCase 구현

**위치**: `domain/usecase/src/main/kotlin/ktc/cargo/driver/domain/usecase/{feature}/`

**규칙**:
- 패키지: `ktc.cargo.driver.domain.usecase.{feature}`
- `class` + `@Inject constructor`로 Repository 주입
- `operator fun invoke()` 사용
- 단일 책임 원칙 (하나의 UseCase = 하나의 비즈니스 기능)
- Validation을 가장 먼저 수행
- 여러 Repository 조합 가능

**네이밍**: 비즈니스 행위를 명확히 전달하는 이름
- 예: `LoginUseCase`, `CheckLoginRequirementUseCase`, `LogoutUseCase`

**참고 패턴**:
```kotlin
package ktc.cargo.driver.domain.usecase.login

import ktc.cargo.driver.domain.model.MessageType
import ktc.cargo.driver.domain.repository.LoginRepository
import ktc.cargo.driver.domain.repository.TokenRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
	private val loginRepository: LoginRepository,
	private val tokenRepository: TokenRepository,
) {

	suspend operator fun invoke(
		id: String,
		password: String,
		onSuccess: () -> Unit,
		onError: (MessageType) -> Unit,
	) {
		// 1. Validation
		if (id.isEmpty()) {
			onError(MessageType.Message("아이디를 입력해주세요."))
			return
		}

		// 2. Repository 조합
		loginRepository.idLogin(id, password, onError = onError)
			.collect { loginData ->
				tokenRepository.updateToken(loginData.token)
				onSuccess()
			}
	}
}
```

**Flow 반환 UseCase**: Repository의 Flow를 변환/가공할 때
```kotlin
class GetBlockedListUseCase @Inject constructor(
	private val blockRepository: BlockRepository,
) {

	operator fun invoke(
		onError: (MessageType) -> Unit,
	): Flow<List<BlockedCorp>> {
		return blockRepository.getBlockedList(onError = onError)
	}
}
```

**참고 파일**:
- `domain/usecase/src/main/kotlin/ktc/cargo/driver/domain/usecase/login/LoginUseCase.kt`
- `domain/usecase/src/main/kotlin/ktc/cargo/driver/domain/usecase/login/CheckLoginRequirementUseCase.kt`
- `domain/usecase/src/main/kotlin/ktc/cargo/driver/domain/usecase/login/LogoutUseCase.kt`

## Domain Layer 의존성 규칙

### 사용 가능
- Kotlin 표준 라이브러리
- Kotlinx Coroutines (Flow, suspend)
- Javax Inject (`@Inject`)
- 다른 Domain 모듈 (`:domain:model`, `:domain:repository`)

### 사용 금지
- Android Framework (Context, View, Activity 등)
- Data Layer 구현체 (RepositoryImpl, DataSource 등)
- DTO (Response, Request 등)
- Entity (Room Entity 등)
- UI Layer (ViewModel, Composable 등)

## 체크리스트

구현 완료 후 확인:
- [ ] Domain Model이 순수 Kotlin인지 (Android 의존성 없음)
- [ ] Repository Interface가 Domain Model만 사용하는지
- [ ] UseCase에 `@Inject constructor` 있는지
- [ ] UseCase에 `operator fun invoke()` 사용하는지
- [ ] Validation이 UseCase에서 수행되는지
- [ ] `onError: (MessageType) -> Unit` 콜백이 포함되어 있는지
