---
description: "아키텍처 규칙 위반 코드 리뷰 - MaterialTheme 사용 금지, launchIn 금지, Domain 의존성 위반, 컨벤션 검사"
model: claude-sonnet-4-5-20250929
user-invocable: true
allowed-tools:
  - Read
  - Glob
  - Grep
---

# 아키텍처 검증 Skill

$ARGUMENTS 에 명시된 범위(특정 feature, 전체 등)에서 아키텍처 규칙 위반을 검사합니다.

인자가 없으면 최근 변경된 파일 또는 전체 프로젝트를 대상으로 검사합니다.

## 검사 항목

### 1. MaterialTheme 사용 금지

**규칙**: 모든 UI 코드에서 `MaterialTheme.colorScheme` 사용 금지. `KTCTheme.colorScheme`만 허용.

**검사 방법**:
```
Grep: "MaterialTheme\.colorScheme" in feature/**/*, core/ui/**/*, core/designsystem/**/*
```

**예외**: `Theme.kt` 파일 내 MaterialTheme 래핑 코드는 허용.

**위반 예시**:
```kotlin
// ❌ 위반
color = MaterialTheme.colorScheme.onSurface
```

**수정 방향**: `KTCTheme.colorScheme.text.primary` 등으로 변경.

---

### 2. HmmColor 직접 사용 금지

**규칙**: `HmmColor`를 Composable 코드에서 직접 참조 금지.

**검사 방법**:
```
Grep: "HmmColor\." in feature/**/*.kt
```

**예외**: `Color.kt`, `Theme.kt` 등 디자인 시스템 정의 파일은 허용.

---

### 3. launchIn 패턴 금지

**규칙**: ViewModel에서 Flow 수집 시 `.onEach { }.launchIn()` 대신 `viewModelScope.launch { flow.collect { } }` 사용.

**검사 방법**:
```
Grep: "\.launchIn\(" in feature/**/*.kt
```

**위반 예시**:
```kotlin
// ❌ 위반
someFlow.onEach { handleValue(it) }.launchIn(viewModelScope)
```

**수정 방향**:
```kotlin
// ✅ 올바름
viewModelScope.launch {
    someFlow.collect { value -> handleValue(value) }
}
```

---

### 4. 수동 is 타입 체크 금지 (ViewModel)

**규칙**: ViewModel에서 StateFlow 상태 타입 체크 시 수동 `is` 체크 대신 `withData`/`updateWithData` 유틸 사용.

**검사 방법**:
```
Grep: "\.value\s*\n.*!is\s" 또는 "val.*=.*_ui.*\.value\s*\n.*if.*!is" in feature/**/*ViewModel.kt
Grep: "if\s*\(.*State\s+!is\s" in feature/**/*ViewModel.kt
```

**위반 예시**:
```kotlin
// ❌ 위반
val currentState = _uiState.value
if (currentState !is UiState.Data) return
```

**수정 방향**:
```kotlin
// ✅ withData (읽기 전용)
_uiState.withData<UiState.Data> { state ->
    doSomething(state.value)
}

// ✅ updateWithData (상태 업데이트)
_uiState.updateWithData<UiState, UiState.Data> { state ->
    state.copy(isLoading = true)
}
```

---

### 5. MutableStateFlow .value 직접 할당 금지

**규칙**: ViewModel에서 MutableStateFlow 상태 변경 시 `.value =` 대신 `.update { }` 사용. `update`는 thread-safe.

**검사 방법**:
```
Grep: "_\w+\.(value\s*=)" in feature/**/*ViewModel.kt
```

**예외**: `MutableStateFlow`가 아닌 일반 `mutableStateOf` (Compose State)의 `.value =`는 허용.

**위반 예시**:
```kotlin
// ❌ 위반
_uiState.value = UiState.Loading
_modalEffect.value = ModalEffect.Hidden
```

**수정 방향**:
```kotlin
// ✅ 올바름
_uiState.update { UiState.Loading }
_modalEffect.update { ModalEffect.Hidden }
```

---

### 6. ViewModel 함수 on 접두어 금지

**규칙**: ViewModel 함수에 `on` 접두어 사용하지 않음. `on`은 Composable 콜백 파라미터에만 사용.

**검사 방법**:
```
Grep: "fun on[A-Z]" in feature/**/*ViewModel.kt
```

**예외**: `onCleared()` 등 Android 프레임워크 오버라이드 함수는 허용.

**위반 예시**:
```kotlin
// ❌ 위반 (ViewModel)
fun onTabSelected(tab: Tab) { }
```

**수정 방향**:
```kotlin
// ✅ 올바름 (ViewModel)
fun selectTab(tab: Tab) { }
```

---

### 7. Domain Layer 의존성 위반

**규칙**: Domain 모듈(`domain/model`, `domain/repository`, `domain/usecase`)에서 Data Layer, Android Framework, UI Layer를 import하면 안 됨.

**검사 방법**:
```
Grep: "import android\." in domain/**/*.kt
Grep: "import ktc.cargo.driver.data\." in domain/**/*.kt
Grep: "import ktc.cargo.driver.remote\." in domain/**/*.kt
Grep: "import ktc.cargo.driver.local\." in domain/**/*.kt
Grep: "import androidx\." in domain/**/*.kt (javax.inject 제외)
```

**예외**:
- `javax.inject.Inject`는 허용
- `kotlinx.coroutines`, `kotlinx.serialization`은 허용

---

### 8. 하드코딩 색상 사용

**규칙**: Composable 코드에서 `Color(0x...)` 하드코딩 금지 (TODO 주석이 있는 경우 제외).

**검사 방법**:
```
Grep: "Color\(0x" in feature/**/*.kt
```

해당 라인 위에 `TODO: 새로운 색상 필요` 주석이 있으면 허용.

---

## 검사 결과 형식

검사 결과를 다음 형식으로 보고합니다:

```
## 아키텍처 검증 결과

### ✅ 통과 항목
- MaterialTheme 사용: 위반 없음
- launchIn 패턴: 위반 없음

### ❌ 위반 항목

#### [위반 규칙명]
- **파일**: `feature/notice/NoticeViewModel.kt:42`
- **내용**: `someFlow.onEach { }.launchIn(viewModelScope)`
- **수정 방향**: `viewModelScope.launch { someFlow.collect { } }`로 변경

### 📊 요약
- 검사 항목: 8개
- 통과: 6개
- 위반: 2개
```

## 검사 범위 옵션

- `$ARGUMENTS`가 특정 feature명이면 해당 feature 모듈만 검사
  - 예: `/review-architecture notice` → `feature/notice/**/*.kt`만 검사
- `$ARGUMENTS`가 없거나 `all`이면 전체 검사
- `$ARGUMENTS`가 `domain`이면 domain 의존성 위반만 검사
