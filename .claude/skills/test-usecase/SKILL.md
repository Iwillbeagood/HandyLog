---
description: "UseCase 단위 테스트 코드 작성 - Repository Mock, Flow 검증, Validation 테스트 (MockK, Turbine, Coroutines Test)"
model: claude-sonnet-4-5-20250929
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash
---

# UseCase Test Skill

$ARGUMENTS 를 기반으로 UseCase 단위 테스트를 작성합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **테스트 대상**: UseCase 클래스명 또는 기능명 (예: `LoginUseCase`, `GetCargoListUseCase`)

인자가 불충분한 경우 사용자에게 질문합니다.

## 사전 준비

### 1단계: 대상 UseCase 분석

테스트를 작성하기 전에 반드시 대상 UseCase 파일을 읽고 다음을 파악합니다:
- 주입받는 Repository/UseCase 의존성
- `operator fun invoke()` 파라미터와 반환 타입
- Validation 로직 (분기 조건)
- 비즈니스 로직 (데이터 변환, 필터링, 조합)
- `Flow` 반환 여부
- 콜백 파라미터 (`onSuccess`, `onError` 등)

**대상 파일 위치**: `domain/usecase/src/main/kotlin/ktc/cargo/driver/domain/usecase/{feature}/`

### 2단계: build.gradle.kts 의존성 확인

`domain/usecase/build.gradle.kts`에 테스트 의존성이 있는지 확인합니다.

**필수 의존성** (없으면 추가):
```kotlin
dependencies {
	// 기존 의존성...
	testImplementation(libs.mockk)
	testImplementation(libs.turbine)
}
```

> `hmm-jvm-library` 플러그인이 `coroutines.test`와 `kotlin.test`를 자동 포함하므로 별도 추가 불필요합니다.

## 테스트 파일 작성

### 파일 위치

`domain/usecase/src/test/kotlin/ktc/cargo/driver/domain/usecase/{feature}/{UseCaseName}Test.kt`

### 기본 구조

```kotlin
package ktc.cargo.driver.domain.usecase.{feature}

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ktc.cargo.driver.domain.model.MessageType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class {UseCaseName}Test {

	// === Mock 의존성 ===
	private lateinit var someRepository: SomeRepository
	private lateinit var useCase: {UseCaseName}

	@Before
	fun setup() {
		someRepository = mockk(relaxed = true)
		useCase = {UseCaseName}(
			someRepository = someRepository,
		)
	}

	// === 테스트 메서드 ===
}
```

---

## 테스트 패턴

### 패턴 1: suspend fun UseCase (콜백 기반)

UseCase가 `suspend operator fun invoke()`이고 콜백(`onSuccess`, `onError`)을 사용하는 경우:

```kotlin
@Test
fun `빈 아이디로 로그인 시 에러 메시지를 반환한다`() = runTest {
	// given
	val errorSlot = slot<MessageType>()
	val onError: (MessageType) -> Unit = { errorSlot.captured = it }
	val onSuccess: () -> Unit = mockk(relaxed = true)

	// when
	useCase(
		id = "",
		password = "password",
		onSuccess = onSuccess,
		onError = onError,
	)

	// then
	assertTrue(errorSlot.isCaptured)
	assertEquals(
		MessageType.Message("아이디를 입력해주세요."),
		errorSlot.captured,
	)
	coVerify(exactly = 0) { someRepository.login(any(), any(), any()) }
}

@Test
fun `유효한 입력으로 로그인 시 성공 콜백이 호출된다`() = runTest {
	// given
	val expectedData = LoginData(userId = "1", token = "token")
	coEvery {
		someRepository.idLogin(any(), any(), any(), onError = any())
	} returns flowOf(expectedData)

	var successCalled = false

	// when
	useCase(
		id = "testUser",
		password = "testPass",
		onSuccess = { successCalled = true },
		onError = { },
	)

	// then
	assertTrue(successCalled)
	coVerify { tokenRepository.updateToken("token") }
}
```

### 패턴 2: Flow 반환 UseCase

UseCase가 `operator fun invoke(): Flow<T>`를 반환하는 경우, **Turbine**으로 테스트합니다:

```kotlin
@Test
fun `알람 리스트에서 pushUsed가 하나라도 있으면 isOnOff가 true`() = runTest {
	// given
	val alarmList = listOf(
		CargoAlarmInfo(pushUsed = true),
		CargoAlarmInfo(pushUsed = false),
	)
	every {
		cargoAlarmRepository.getCargoAlarmList(onError = any())
	} returns flowOf(alarmList)

	// when & then
	useCase(onError = {}).test {
		val result = awaitItem()
		assertTrue(result.isOnOff)
		assertTrue(result.hasCargoAlarm)
		awaitComplete()
	}
}

@Test
fun `빈 알람 리스트면 isOnOff false, hasCargoAlarm false`() = runTest {
	// given
	every {
		cargoAlarmRepository.getCargoAlarmList(onError = any())
	} returns flowOf(emptyList())

	// when & then
	useCase(onError = {}).test {
		val result = awaitItem()
		assertEquals(false, result.isOnOff)
		assertEquals(false, result.hasCargoAlarm)
		awaitComplete()
	}
}
```

### 패턴 3: 단순 Boolean 반환 UseCase

```kotlin
@Test
fun `숨김 화물이 30개 이상이면 true를 반환한다`() = runTest {
	// given
	val hiddenOrders = List(30) { HiddenOrder(orderNum = "order$it") }
	coEvery { hiddenOrderRepository.getHiddenOrders() } returns hiddenOrders

	// when
	val result = useCase()

	// then
	assertTrue(result)
}

@Test
fun `숨김 화물이 30개 미만이면 false를 반환한다`() = runTest {
	// given
	val hiddenOrders = List(29) { HiddenOrder(orderNum = "order$it") }
	coEvery { hiddenOrderRepository.getHiddenOrders() } returns hiddenOrders

	// when
	val result = useCase()

	// then
	assertEquals(false, result)
}
```

### 패턴 4: 여러 Repository 조합 UseCase (combine)

```kotlin
@Test
fun `설정과 알람 상태를 조합하여 반환한다`() = runTest {
	// given
	val setting = Setting(/* ... */)
	val alarmOnOff = CargoAlarmOnOff(isOnOff = true, hasCargoAlarm = true)

	every { settingRepository.observeSetting() } returns flowOf(setting)
	every {
		getCargoAlarmStatusUseCase(onError = any())
	} returns flowOf(alarmOnOff)

	// when & then
	useCase(onError = {}).test {
		val (resultSetting, resultAlarm) = awaitItem()
		assertEquals(setting, resultSetting)
		assertEquals(alarmOnOff, resultAlarm)
		awaitComplete()
	}
}
```

### 패턴 5: 필터링/변환 로직이 있는 UseCase

```kotlin
@Test
fun `차단 업체의 화물은 필터링된다`() = runTest {
	// given
	val blockedCorp = BlockedCorp(seq = 100)
	val cargoList = listOf(
		Cargo(corpSeq = 100, info = "화물1"),  // 차단 업체
		Cargo(corpSeq = 200, info = "화물2"),  // 정상 업체
	)

	every {
		cargoRepository.getCargoList(any(), any(), any(), any(), any(), any(), onError = any())
	} returns flowOf(cargoList)
	every {
		blockRepository.getBlockedList(onError = any())
	} returns flowOf(listOf(blockedCorp))

	// when & then
	useCase(cargoSetting = cargoSetting, onError = {}).test {
		val result = awaitItem()
		assertEquals(1, result.size)
		assertEquals(200, result[0].corpSeq)
		awaitComplete()
	}
}
```

---

## 콜백 검증 기법

### slot으로 콜백 인자 캡처
```kotlin
val errorSlot = slot<MessageType>()
val onError: (MessageType) -> Unit = { errorSlot.captured = it }

// 실행 후 검증
assertTrue(errorSlot.isCaptured)
assertEquals(MessageType.Message("예상 메시지"), errorSlot.captured)
```

### Boolean 플래그로 호출 여부 확인
```kotlin
var successCalled = false
useCase(onSuccess = { successCalled = true }, onError = {})
assertTrue(successCalled)
```

### mockk(relaxed = true)로 호출 검증
```kotlin
val onSuccess: () -> Unit = mockk(relaxed = true)
useCase(onSuccess = onSuccess, onError = {})
coVerify(exactly = 1) { onSuccess() }
```

---

## MockK 사용 규칙

### suspend fun 모킹
```kotlin
coEvery { repository.getData() } returns expectedData
coVerify { repository.getData() }
```

### 일반 fun 모킹 (Flow 반환)
```kotlin
every { repository.observeData(onError = any()) } returns flowOf(data)
verify { repository.observeData(onError = any()) }
```

### relaxed mock
```kotlin
// 모든 함수가 기본값 반환 (Unit, 0, "", false, emptyList 등)
private val repository: SomeRepository = mockk(relaxed = true)
```

### any() 매처
```kotlin
coEvery { repository.login(id = any(), password = any(), onError = any()) } returns flowOf(data)
```

---

## 테스트 네이밍 규칙

한글 백틱 네이밍을 사용합니다:

```kotlin
@Test
fun `빈 아이디로 로그인 시 에러 메시지를 반환한다`() = runTest { }

@Test
fun `유효한 입력으로 로그인 시 토큰이 저장된다`() = runTest { }

@Test
fun `차단 키워드가 포함된 화물은 목록에서 제외된다`() = runTest { }

@Test
fun `숨김 화물이 제한 개수 이상이면 true를 반환한다`() = runTest { }
```

**네이밍 형식**: `` `{조건}일 때 {기대 결과}` `` 또는 `` `{행위} 시 {기대 결과}` ``

---

## 테스트 케이스 도출 가이드

UseCase를 분석할 때 다음 관점에서 테스트 케이스를 도출합니다:

### 필수 테스트
1. **정상 케이스 (Happy Path)**: 유효한 입력으로 기대 결과가 나오는지
2. **Validation 실패**: 빈 값, 잘못된 형식 등에서 onError가 호출되는지
3. **콜백 호출 검증**: onSuccess/onError 중 올바른 콜백이 호출되는지

### 권장 테스트
4. **경계값 (Boundary)**: 제한 개수, 빈 리스트 등
5. **분기 로직**: when/if 분기마다 올바른 경로를 타는지
6. **데이터 변환**: 입력 데이터가 올바르게 변환되는지
7. **여러 Repository 조합**: combine 결과가 정확한지

### 선택 테스트
8. **에러 전파**: Repository에서 에러가 발생할 때 onError로 전파되는지
9. **호출 검증**: 특정 조건에서 Repository 함수가 호출되지 않는지

---

## 체크리스트

테스트 작성 완료 후 확인:
- [ ] `domain/usecase/build.gradle.kts`에 `testImplementation(libs.mockk)`과 `testImplementation(libs.turbine)` 추가
- [ ] 테스트 파일 위치: `domain/usecase/src/test/kotlin/...`
- [ ] 모든 Repository 의존성이 mockk로 생성되었는지
- [ ] `@Before`에서 UseCase 인스턴스가 생성되었는지
- [ ] suspend UseCase는 `runTest { }` 안에서 실행되는지
- [ ] Flow 반환 UseCase는 `.test { }` (Turbine)으로 검증하는지
- [ ] Validation 분기마다 테스트가 있는지
- [ ] Happy Path 테스트가 있는지
- [ ] 테스트 메서드명이 한글 백틱 네이밍인지
