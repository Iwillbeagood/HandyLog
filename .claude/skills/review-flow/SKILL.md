---
description: "ViewModel의 Flow 파이프라인 코드 리뷰 - 파생 Flow 구조, stateIn 구독 전략, 상태 관리 패턴 검증"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Glob
  - Grep
---

# Flow 파이프라인 검증 Skill

$ARGUMENTS 에 명시된 범위(특정 feature, 전체 등)에서 ViewModel의 Flow 파이프라인이 올바르게 구성되어 있는지 검증합니다.

인자가 없으면 전체 ViewModel을 대상으로 검사합니다.

---

## 검사 항목

### 1. flatMapLatest 사용 여부

**규칙**: 필터/검색 조건에 따른 데이터 소스 전환 시 반드시 `flatMapLatest`를 사용해야 합니다. `flatMapMerge`나 `flatMapConcat`은 이전 Flow가 취소되지 않아 데이터 불일치가 발생합니다.

**검사 방법**:
```
Grep: "flatMapMerge\|flatMapConcat" in feature/**/*ViewModel.kt
```

**위반 예시**:
```kotlin
// ❌ 위반 - 이전 필터의 Flow가 취소되지 않음
filterState.flatMapMerge { filter ->
    repository.getList(filter)
}
```

**수정 방향**: `flatMapLatest`로 변경

---

### 2. triggerStateIn 필요성 검증

**규칙**: mutation(삭제, 수정 등) 후 재조회가 필요한 ViewModel은 `triggerStateIn`을 사용해야 합니다. `stateIn`을 사용하면 `restart()`를 호출할 수 없습니다.

**검사 방법**:
```
1. Grep: "\.restart\(\)" in feature/**/*ViewModel.kt → restart 호출하는 VM 파악
2. 해당 VM에서 uiState 선언이 triggerStateIn인지 확인
3. Grep: "Repository\.\(delete\|update\|create\|add\|remove\|save\)" in feature/**/*ViewModel.kt
   → mutation 함수가 있는데 restart()가 없으면 경고
```

**위반 예시**:
```kotlin
// ❌ 위반 - mutation이 있는데 stateIn 사용
val uiState: StateFlow<UiState> = repository.getItems()
    .map { ... }
    .stateIn(...)  // restart 불가!

fun deleteItem(id: String) {
    viewModelScope.launch {
        repository.deleteItem(id)
        // 재조회 방법 없음!
    }
}
```

**수정 방향**: `stateIn` → `triggerStateIn`으로 변경하고 mutation 후 `uiState.restart()` 호출

---

### 3. SharingStarted 전략 검증

**규칙**: 각 사용 시점에 맞는 SharingStarted 전략을 사용해야 합니다.

**검사 방법**:
```
Grep: "stateIn\(|triggerStateIn\(" in feature/**/*ViewModel.kt
→ 각 호출의 started 파라미터 확인
```

**검증 기준**:

| 사용 시점 | 올바른 전략 | 위반 |
|-----------|------------|------|
| 일반 목록/상세 화면 | `WhileSubscribed(5000)` 또는 `WhileSubscribed(3000)` | `Eagerly` (리소스 낭비) |
| 필터 StateFlow (Eagerly 공유) | `Eagerly` 또는 `WhileSubscribed` | - |
| 수동 트리거 + init 패턴 | `Lazily` | `WhileSubscribed` (trigger 유실 가능) |

**위반 예시**:
```kotlin
// ❌ 위반 - 대량 데이터를 Eagerly로 공유 (메모리 낭비)
val uiState = repository.getHeavyList()
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

---

### 4. combine 내부 Flow가 모두 reactive인지

**규칙**: `combine`에 전달되는 모든 Flow가 실제로 값을 emit하는 reactive Flow여야 합니다. cold Flow가 한 번만 emit하고 완료되면 combine이 더 이상 반응하지 않습니다.

**검사 방법**: 수동 코드 리뷰 (자동 Grep 어려움)
- combine 내부의 각 Flow 소스를 추적하여 모두 `Flow<T>` (지속적 emit)인지 확인
- 일회성 `suspend fun`을 Flow로 래핑하지 않았는지 확인

**위반 예시**:
```kotlin
// ❌ 위반 - suspend fun을 flow { }로 래핑하면 한번만 emit
combine(
    flow { emit(repository.getData()) },  // 한 번만 emit
    repository.observeUpdates(),           // 지속적 emit
) { data, updates -> ... }
```

**수정 방향**: 일회성 데이터는 combine 밖에서 별도 처리하거나 Repository에서 `Flow<T>`로 반환

---

### 5. 페이지네이션 누적 리스트 패턴 검증

**규칙**: 페이지네이션 구현 시 `accumulativeListState`(또는 유사 이름)를 사용하여 리스트를 누적해야 합니다.

**검사 방법**:
```
Grep: "nextPage\|loadMore\|loadNext" in feature/**/*ViewModel.kt → 페이지네이션 VM 파악
→ 해당 VM에서 accumulative/accum 패턴 존재 여부 확인
Grep: "accum" in feature/**/*ViewModel.kt
```

**검증 기준**:
- `MutableStateFlow<List<T>?>` 형태의 누적 저장소가 있는지
- 필터 변경 시 누적 리스트가 초기화되는지
- 페이지 추가 시 기존 리스트에 append하는지

**위반 예시**:
```kotlin
// ❌ 위반 - 페이지 변경 시 이전 데이터 유실
page.flatMapLatest { p ->
    repository.getList(page = p)
}.map { items ->
    // 이전 페이지 데이터가 없음!
    UiState.Data(items = items)
}
```

---

### 6. onError 콜백 전달 검증

**규칙**: Repository/UseCase 호출 시 `onError` 콜백을 전달하여 에러를 UI에 표시해야 합니다.

**검사 방법**:
```
Grep: "Repository\.\w+\(" in feature/**/*ViewModel.kt
→ 각 호출에 onError 파라미터 존재 여부 확인
```

**위반 예시**:
```kotlin
// ❌ 위반 - onError 누락으로 에러가 무시됨
val uiState = repository.getList()
    .map { ... }
    .triggerStateIn(...)
```

**수정 방향**:
```kotlin
// ✅ onError 전달
val uiState = repository.getList(onError = ::showMessage)
    .map { ... }
    .triggerStateIn(...)
```

---

### 7. MutableStateFlow .value 직접 할당 금지

**규칙**: ViewModel에서 MutableStateFlow 상태 변경 시 `.value =` 대신 `.update { }` 사용.

**검사 방법**:
```
Grep: "_\w+\.(value\s*=)" in feature/**/*ViewModel.kt
```

**예외**: `MutableStateFlow`가 아닌 일반 `mutableStateOf`(Compose State)의 `.value =`는 허용.

---

### 8. Flow 수집 패턴 검증

**규칙**: `.onEach { }.launchIn()` 대신 `viewModelScope.launch { flow.collect { } }` 사용.

**검사 방법**:
```
Grep: "\.launchIn\(" in feature/**/*ViewModel.kt
```

---

### 9. 수동 is 타입 체크 금지

**규칙**: `withData<T>` / `updateWithData<State, T>` 유틸 사용.

**검사 방법**:
```
Grep: "\.value\s*$" in feature/**/*ViewModel.kt (다음 줄에 !is 체크가 오는 패턴)
Grep: "if\s*\(.*State\s+!is\s" in feature/**/*ViewModel.kt
```

---

### 10. viewModelScope 누락 검증

**규칙**: suspend 함수 호출 시 반드시 `viewModelScope.launch { }` 내부에서 호출해야 합니다.

**검사 방법**: 수동 코드 리뷰
- ViewModel의 public 함수가 직접 suspend 함수를 호출하지 않는지 확인
- `GlobalScope` 사용이 없는지 확인

```
Grep: "GlobalScope" in feature/**/*ViewModel.kt
```

---

## 검사 결과 형식

```
## Flow 파이프라인 검증 결과

### ✅ 통과 항목
- flatMapLatest 사용: 위반 없음
- SharingStarted 전략: 적절함
- onError 콜백: 모두 전달됨

### ❌ 위반 항목

#### [위반 규칙명]
- **파일**: `feature/xxx/XxxViewModel.kt:42`
- **내용**: `stateIn 사용 중이지만 mutation 후 restart 필요`
- **수정 방향**: `triggerStateIn`으로 변경 + `uiState.restart()` 추가

### ⚠️ 경고 항목 (수동 확인 필요)

#### combine 내부 Flow reactivity
- **파일**: `feature/xxx/XxxViewModel.kt:30`
- **내용**: `combine 내 3개 Flow 중 1개가 일회성 emit일 가능성`
- **확인 필요**: Repository의 해당 함수가 Flow<T>를 반환하는지 확인

### 📊 요약
- 검사 항목: 10개
- 자동 검사 통과: 7개
- 위반: 1개
- 경고 (수동 확인): 2개
```

---

## 검사 범위 옵션

- `$ARGUMENTS`가 특정 feature명이면 해당 feature 모듈만 검사
  - 예: `/review-flow notice` → `feature/notice/**/*ViewModel.kt`만 검사
- `$ARGUMENTS`가 없거나 `all`이면 전체 검사
- `$ARGUMENTS`가 특정 파일 경로이면 해당 파일만 검사

---

## 참고 파일

- **FlowUtil**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/flow/FlowUtil.kt`
- **TriggerStateFlow**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/trigger/TriggerStateFlow.kt`
- **triggerStateIn**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/trigger/TriggerStateIn.kt`
- **RefreshableViewModel**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/viewmodel/RefreshableViewModel.kt`
