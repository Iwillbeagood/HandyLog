---
description: "ViewModel 단위 테스트 코드 작성 - StateFlow 상태 변화, UiEffect, ModalEffect, 페이지네이션 테스트 (MockK, Turbine)"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash
---

# ViewModel Test Skill

$ARGUMENTS 를 기반으로 ViewModel 단위 테스트를 작성합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **테스트 대상**: ViewModel 클래스명 또는 기능명 (예: `LoginViewModel`, `CargoListViewModel`)

인자가 불충분한 경우 사용자에게 질문합니다.

## 사전 준비

### 1단계: 대상 ViewModel 분석

테스트를 작성하기 전에 반드시 대상 ViewModel 파일과 관련 State/Effect 파일을 읽고 다음을 파악합니다:
- 주입받는 UseCase/Repository 의존성
- SavedStateHandle 사용 여부
- uiState 타입과 파생 Flow 패턴 (A/B/C/D/E 중 무엇인지)
- ModalEffect / UiEffect 존재 여부
- 공개 함수 목록 (사용자 인터랙션)
- init 블록의 초기화 로직

**대상 파일 위치**: `feature/{feature}/src/main/kotlin/ktc/cargo/driver/{feature}/`

**함께 읽어야 할 파일**:
- `contract/{Name}State.kt` (UiState 정의)
- `contract/{Name}Effect.kt` (UiEffect, ModalEffect 정의)

### 2단계: 테스트 의존성 확인

`hmm-android-feature` 플러그인이 자동으로 다음을 포함합니다:
- `testImplementation(project(":core:testing"))` → JUnit4, MockK, Turbine, Coroutines Test
- `testImplementation(libs.androidx.navigation.testing)`

별도의 build.gradle.kts 수정은 **불필요**합니다.

## 테스트 파일 작성

### 파일 위치

`feature/{feature}/src/test/kotlin/ktc/cargo/driver/{feature}/{ViewModelName}Test.kt`

> 패키지 경로는 대상 ViewModel의 패키지와 동일하게 맞춥니다.

### 기본 구조

```kotlin
package ktc.cargo.driver.{feature}

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ktc.cargo.driver.testing.rule.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class {ViewModelName}Test {

	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	// === Mock 의존성 ===
	private lateinit var someUseCase: SomeUseCase
	private lateinit var viewModel: {ViewModelName}

	@Before
	fun setup() {
		someUseCase = mockk(relaxed = true)
		// ViewModel 생성은 각 테스트에서 하거나, 공통 setup에서 합니다.
		// init 블록에서 데이터를 로딩하는 ViewModel은 mock 설정 후 생성해야 합니다.
	}

	private fun createViewModel(): {ViewModelName} {
		return {ViewModelName}(
			someUseCase = someUseCase,
		)
	}

	// === 테스트 메서드 ===
}
```

---

## MainDispatcherRule 필수 사용

ViewModel은 `viewModelScope`(Dispatchers.Main)를 사용하므로, **반드시** `MainDispatcherRule`을 선언해야 합니다.

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

**위치**: `core/testing/src/main/kotlin/ktc/cargo/driver/testing/rule/MainDispatcherRule.kt`

이 Rule은 `UnconfinedTestDispatcher`를 사용하여 Main 디스패처를 테스트용으로 교체합니다.

---

## ViewModel 생성 전략

### init에서 데이터를 로딩하는 경우

대부분의 ViewModel은 init 블록에서 Flow 파이프라인을 설정하거나 데이터를 로딩합니다.
mock 설정을 **먼저** 하고 나서 ViewModel을 생성해야 합니다.

```kotlin
@Test
fun `초기 로딩 시 설정 데이터를 가져온다`() = runTest {
	// given - mock 설정을 먼저
	every {
		getMySettingUseCase(onError = any())
	} returns flowOf(Pair(setting, alarmOnOff))

	// when - ViewModel 생성 (init 실행)
	viewModel = createViewModel()

	// then
	viewModel.uiState.test {
		val state = awaitItem()
		assertIs<SettingState.Data>(state)
	}
}
```

### SavedStateHandle이 필요한 경우

`SavedStateHandle`을 사용하는 ViewModel은 테스트에서 직접 생성합니다:

```kotlin
private fun createViewModel(
	phoneNumber: String? = null,
): LoginViewModel {
	val savedStateHandle = SavedStateHandle().apply {
		// Route의 파라미터를 직접 설정
		// Navigation의 toRoute()가 내부적으로 읽는 키를 세팅
	}
	return LoginViewModel(
		loginUseCase = loginUseCase,
		savedStateHandle = savedStateHandle,
	)
}
```

> SavedStateHandle의 키는 Navigation Route의 프로퍼티명과 매칭됩니다.

---

## 테스트 패턴

### 패턴 1: MutableStateFlow 상태 변경 테스트 (패턴 E)

`MutableStateFlow`를 직접 업데이트하는 ViewModel 패턴:

```kotlin
@Test
fun `전화번호 입력 시 상태가 업데이트된다`() = runTest {
	// given
	viewModel = createViewModel()

	// when
	viewModel.numberValueChange("01012345678")

	// then
	val state = viewModel.loginState.value
	assertIs<LoginState.Data>(state)
	assertEquals("01012345678", state.phoneNumber)
}

@Test
fun `12자리 초과 입력은 무시된다`() = runTest {
	// given
	viewModel = createViewModel()

	// when
	viewModel.numberValueChange("0101234567890")  // 13자리

	// then
	val state = viewModel.loginState.value
	assertIs<LoginState.Data>(state)
	assertEquals("", state.phoneNumber)  // 변경되지 않음
}
```

### 패턴 2: TriggerStateFlow / stateIn 파이프라인 테스트 (패턴 A~D)

파생 Flow 파이프라인으로 생성된 `uiState`는 Turbine으로 테스트합니다:

```kotlin
@Test
fun `목록 로딩 시 Data 상태로 전환된다`() = runTest {
	// given
	val items = listOf(Item("1"), Item("2"))
	every {
		repository.getItems(onError = any())
	} returns flowOf(items)

	viewModel = createViewModel()

	// when & then
	viewModel.uiState.test {
		// initialValue가 Loading일 수 있음
		skipItems(1)  // Loading 스킵 (필요시)

		val state = awaitItem()
		assertIs<ListUiState.Data>(state)
		assertEquals(2, state.items.size)
	}
}

@Test
fun `빈 목록이면 Empty 상태가 된다`() = runTest {
	// given
	every {
		repository.getItems(onError = any())
	} returns flowOf(emptyList())

	viewModel = createViewModel()

	// when & then
	viewModel.uiState.test {
		val state = expectMostRecentItem()
		assertIs<ListUiState.Empty>(state)
	}
}
```

### 패턴 3: SharedFlow (UiEffect) 테스트

일회성 이벤트는 `SharedFlow`로 방출됩니다:

```kotlin
@Test
fun `로그인 성공 시 LoginComplete 이벤트가 방출된다`() = runTest {
	// given
	setupLoginSuccess()
	viewModel = createViewModel()

	// when & then
	viewModel.loginEffect.test {
		viewModel.login()

		val effect = awaitItem()
		assertIs<LoginEffect.LoginComplete>(effect)
	}
}

@Test
fun `에러 발생 시 ShowToast 이벤트가 방출된다`() = runTest {
	// given
	viewModel = createViewModel()

	// when & then
	viewModel.loginEffect.test {
		viewModel.showToast(MessageType.Message("에러 메시지"))

		val effect = awaitItem()
		assertIs<LoginEffect.ShowToast>(effect)
		assertEquals(
			MessageType.Message("에러 메시지"),
			(effect as LoginEffect.ShowToast).message,
		)
	}
}
```

### 패턴 4: ModalEffect (StateFlow) 테스트

모달 상태는 `StateFlow`이므로 `.value`로 직접 검증합니다:

```kotlin
@Test
fun `카카오 미등록 에러 시 모달이 표시된다`() = runTest {
	// given
	viewModel = createViewModel()

	// when
	viewModel.showKakaoUnregisterError()

	// then
	assertIs<LoginModalEffect.ShowKakaoUnregisterDialog>(
		viewModel.loginModalEffect.value,
	)
}

@Test
fun `모달 숨기기 시 Hidden 상태가 된다`() = runTest {
	// given
	viewModel = createViewModel()
	viewModel.showKakaoUnregisterError()  // 먼저 모달 표시

	// when
	viewModel.hideModal()

	// then
	assertIs<LoginModalEffect.Hidden>(viewModel.loginModalEffect.value)
}
```

### 패턴 5: 필터 변경 → 상태 갱신 테스트

필터 상태가 변경되면 `flatMapLatest`로 데이터가 재조회되는 패턴:

```kotlin
@Test
fun `필터 변경 시 새로운 데이터가 로딩된다`() = runTest {
	// given
	val filteredItems = listOf(Item("filtered"))
	every {
		repository.getList(startDate = any(), endDate = any(), onError = any())
	} returns flowOf(filteredItems)

	viewModel = createViewModel()

	// when & then
	viewModel.uiState.test {
		viewModel.changeFilter(newStartDate, newEndDate)

		val state = expectMostRecentItem()
		assertIs<ListUiState.Data>(state)
		assertEquals(1, state.items.size)
	}
}
```

### 패턴 6: 페이지네이션 테스트

```kotlin
@Test
fun `다음 페이지 로딩 시 기존 목록에 추가된다`() = runTest {
	// given
	val page1 = List(20) { Item("item$it") }
	val page2 = List(10) { Item("item${it + 20}") }

	every {
		repository.getList(offset = 0, onError = any())
	} returns flowOf(page1)
	every {
		repository.getList(offset = 20, onError = any())
	} returns flowOf(page2)

	viewModel = createViewModel()

	// when & then
	viewModel.uiState.test {
		// 첫 페이지
		val firstState = expectMostRecentItem()
		assertIs<ListUiState.Data>(firstState)
		assertEquals(20, firstState.items.size)

		// 다음 페이지
		viewModel.nextPage()

		val nextState = expectMostRecentItem()
		assertIs<ListUiState.Data>(nextState)
		assertEquals(30, nextState.items.size)  // 20 + 10
	}
}
```

### 패턴 7: Mutation 후 재조회 테스트

```kotlin
@Test
fun `항목 삭제 후 목록이 갱신된다`() = runTest {
	// given
	val beforeDelete = listOf(Item("1"), Item("2"))
	val afterDelete = listOf(Item("2"))

	var callCount = 0
	every {
		repository.getItems(onError = any())
	} answers {
		callCount++
		if (callCount == 1) flowOf(beforeDelete) else flowOf(afterDelete)
	}
	coEvery { repository.deleteItem(any(), onError = any()) } returns Unit

	viewModel = createViewModel()

	// when & then
	viewModel.uiState.test {
		val before = expectMostRecentItem()
		assertIs<ListUiState.Data>(before)
		assertEquals(2, before.items.size)

		viewModel.deleteItem("1")

		val after = expectMostRecentItem()
		assertIs<ListUiState.Data>(after)
		assertEquals(1, after.items.size)
	}
}
```

---

## Turbine 사용 팁

### awaitItem() vs expectMostRecentItem()

```kotlin
// awaitItem(): 다음 방출을 순서대로 하나씩 소비
viewModel.uiState.test {
	val loading = awaitItem()      // 첫 번째 방출 (Loading)
	val data = awaitItem()          // 두 번째 방출 (Data)
	assertIs<ListUiState.Data>(data)
}

// expectMostRecentItem(): 중간 방출을 건너뛰고 최신 값만 확인
viewModel.uiState.test {
	val state = expectMostRecentItem()  // 가장 최근 상태
	assertIs<ListUiState.Data>(state)
}
```

### skipItems(): 관심 없는 방출 건너뛰기

```kotlin
viewModel.uiState.test {
	skipItems(1)  // Loading 상태 스킵
	val data = awaitItem()
	assertIs<ListUiState.Data>(data)
}
```

### cancelAndIgnoreRemainingEvents(): 나머지 무시

```kotlin
viewModel.uiState.test {
	val state = awaitItem()
	// 검증 완료 후 남은 이벤트 무시
	cancelAndIgnoreRemainingEvents()
}
```

---

## UseCase mock 설정 기법

### suspend UseCase (콜백 기반) mock

```kotlin
coEvery {
	loginUseCase(
		loginType = any(),
		aid = any(),
		onSuccess = any(),
		onStartAuthProcess = any(),
		onUnregisterError = any(),
		onError = any(),
	)
} coAnswers {
	// 특정 콜백을 호출하여 성공 시나리오 시뮬레이션
	val onSuccess = arg<() -> Unit>(2)  // 3번째 파라미터 (0-indexed)
	onSuccess()
}
```

### Flow 반환 UseCase mock

```kotlin
every {
	getCargoListUseCase(
		cargoSetting = any(),
		offset = any(),
		limit = any(),
		onError = any(),
	)
} returns flowOf(cargoList)
```

### onError 콜백을 통한 에러 시뮬레이션

```kotlin
coEvery {
	someUseCase(onError = any())
} coAnswers {
	val onError = arg<(MessageType) -> Unit>(0)
	onError(MessageType.Message("서버 에러"))
}
```

---

## 테스트 네이밍 규칙

한글 백틱 네이밍을 사용합니다:

```kotlin
@Test
fun `초기 상태는 Loading이다`() = runTest { }

@Test
fun `전화번호 입력 시 상태가 업데이트된다`() = runTest { }

@Test
fun `로그인 성공 시 LoginComplete 이벤트가 방출된다`() = runTest { }

@Test
fun `카카오 미등록 에러 시 모달이 표시된다`() = runTest { }

@Test
fun `필터 변경 시 목록이 재조회된다`() = runTest { }
```

**네이밍 형식**: `` `{조건/행위}일 때 {기대 결과}` `` 또는 `` `{행위} 시 {기대 결과}` ``

---

## 테스트 케이스 도출 가이드

ViewModel을 분석할 때 다음 관점에서 테스트 케이스를 도출합니다:

### 필수 테스트
1. **초기 상태**: ViewModel 생성 직후 uiState가 올바른지
2. **상태 변경**: 각 공개 함수 호출 후 uiState가 올바르게 변하는지
3. **Effect 방출**: 일회성 이벤트가 올바른 시점에 방출되는지

### 권장 테스트
4. **입력 검증**: 입력값 제한 (길이, 형식 등)이 동작하는지
5. **로딩 상태**: 비동기 작업 중 isLoading이 true/false로 전환되는지
6. **에러 처리**: 에러 발생 시 적절한 Effect가 방출되는지
7. **ModalEffect**: 모달 표시/숨기기가 올바른지

### 선택 테스트
8. **페이지네이션**: 다음 페이지 로딩, 끝 도달 처리
9. **새로고침**: refresh() 호출 시 데이터 재조회
10. **필터링**: 필터 변경 시 데이터 재조회

---

## 체크리스트

테스트 작성 완료 후 확인:
- [ ] `@get:Rule val mainDispatcherRule = MainDispatcherRule()` 선언
- [ ] 테스트 파일 위치: `feature/{feature}/src/test/kotlin/...`
- [ ] 패키지가 대상 ViewModel과 동일한지
- [ ] 모든 UseCase/Repository 의존성이 mockk로 생성되었는지
- [ ] init 블록이 있는 ViewModel은 mock 설정 후 생성하는지
- [ ] `runTest { }` 안에서 테스트가 실행되는지
- [ ] StateFlow 파이프라인은 Turbine `.test { }`로 검증하는지
- [ ] SharedFlow(Effect)는 Turbine `.test { }`로 검증하는지
- [ ] StateFlow(ModalEffect)는 `.value`로 직접 검증하는지
- [ ] 테스트 메서드명이 한글 백틱 네이밍인지
