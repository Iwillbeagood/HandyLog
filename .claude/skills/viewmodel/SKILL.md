---
description: "ViewModel 구현 - StateFlow 파이프라인, 상태 관리, 페이지네이션, TriggerStateFlow, RefreshableViewModel 패턴"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# ViewModel Skill

$ARGUMENTS 를 기반으로 ViewModel을 구현합니다. 이 프로젝트에서 ViewModel은 **파생 Flow 파이프라인**이 핵심이며, 대부분의 `uiState`는 여러 데이터 소스를 조합한 Flow 체인으로 구성됩니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Notice`, `DispatchList`, `CargoList`)
- **데이터 소스**: 사용할 Repository/UseCase
- **상태 구조**: UiState에 포함할 데이터
- **사용자 인터랙션**: 필터링, 페이지네이션, 새로고침 등

인자가 불충분한 경우 사용자에게 질문합니다.

## ViewModel 기본 구조

**위치**: `feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/{ClassName}ViewModel.kt`

```kotlin
@HiltViewModel
internal class {Name}ViewModel @Inject constructor(
	private val someRepository: SomeRepository,
	private val savedStateHandle: SavedStateHandle,  // 필요시
) : ViewModel() {

	// === State ===
	val uiState: TriggerStateFlow<{Name}UiState> = ...  // 파생 Flow

	// === Modal Effect (StateFlow) ===
	private val _modalEffect = MutableStateFlow<{Name}ModalEffect>({Name}ModalEffect.Hidden)
	val modalEffect: StateFlow<{Name}ModalEffect> get() = _modalEffect

	// === Effect (SharedFlow - 일회성) ===
	private val _uiEffect = MutableSharedFlow<{Name}UiEffect>()
	val uiEffect: SharedFlow<{Name}UiEffect> get() = _uiEffect

	// === 함수 (on 접두어 없음) ===
	fun selectTab(tab: Tab) { }
	fun search(query: String) { }
	fun dismiss() {
		_modalEffect.update { {Name}ModalEffect.Hidden }
	}

	private fun showMessage(message: MessageType) {
		viewModelScope.launch {
			_uiEffect.emit({Name}UiEffect.ShowMessage(message))
		}
	}
}
```

---

## 파생 Flow 파이프라인 패턴

이 프로젝트의 핵심 패턴입니다. 상황에 따라 적합한 패턴을 선택합니다.

### 패턴 A: Filter → flatMapLatest → combine → triggerStateIn

**가장 흔한 패턴**. 필터 조건에 따라 여러 데이터 소스를 조합하는 목록 화면에 적합합니다.

```kotlin
private val _filterState = MutableStateFlow(FilterState())
val filterState: StateFlow<FilterState> get() = _filterState

val uiState: TriggerStateFlow<ListUiState> = _filterState
	.flatMapLatest { filter ->
		combine(
			repository.getList(
				startDate = filter.startDate,
				endDate = filter.endDate,
				offset = filter.offset,
				onError = ::showMessage,
			),
			userInfoRepository.getUserInfo(),
		) { list, userInfo ->
			ListUiState.Data(
				items = list,
				userInfo = userInfo,
			)
		}
	}.triggerStateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(),
		initialValue = ListUiState.Loading,
	)
```

**사용 시점**: 필터/검색 조건이 바뀔 때 이전 Flow를 취소하고 새 Flow를 시작해야 할 때

**참고 파일**:
- `feature/dispatch/list/src/.../DispatchListViewModel.kt`

---

### 패턴 B: combine → triggerStateIn

**상세 화면 패턴**. 여러 데이터 소스를 단순 조합할 때 사용합니다.

```kotlin
val uiState: TriggerStateFlow<DetailUiState> = combine(
	repository.getDetail(id, ::showMessage),
	otherRepository.getRelated(id, ::showMessage),
) { detail, related ->
	DetailUiState.Data(
		detail = detail,
		related = related,
	)
}.triggerStateIn(
	scope = viewModelScope,
	started = SharingStarted.WhileSubscribed(3000),
	initialValue = DetailUiState.Loading,
)
```

**사용 시점**: 고정된 파라미터로 여러 소스를 결합하는 상세 화면

**참고 파일**:
- `feature/dispatch/detail/src/.../DispatchDetailViewModel.kt`

---

### 패턴 C: repository → map → triggerStateIn

**단순 목록 패턴**. 단일 데이터 소스를 변환하는 가장 간단한 형태입니다.

```kotlin
val uiState: TriggerStateFlow<ListUiState> = repository.getItems(::showMessage)
	.map { items ->
		if (items.isEmpty()) {
			ListUiState.Empty
		} else {
			ListUiState.Data(items = items)
		}
	}.triggerStateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = ListUiState.Loading,
	)
```

**사용 시점**: 단일 소스의 단순 변환

**참고 파일**:
- `feature/account/favorite/src/.../FavoriteViewModel.kt`

---

### 패턴 D: MutableSharedFlow trigger → flatMapLatest → stateIn

**수동 트리거 패턴**. 특정 액션 시에만 데이터를 갱신해야 할 때 사용합니다.

```kotlin
private val trigger = MutableSharedFlow<Unit>(replay = 1)

val uiState: StateFlow<CardUiState> = trigger.flatMapLatest {
	repository.getItems(::showMessage)
}.map {
	CardUiState.Data(items = it)
}.stateIn(
	scope = viewModelScope,
	started = SharingStarted.Lazily,
	initialValue = CardUiState.Loading,
)

init {
	fetchData()
}

private fun fetchData() {
	viewModelScope.launch {
		trigger.emit(Unit)
	}
}
```

**사용 시점**: `triggerStateIn`을 쓰지 않고 수동으로 갱신 제어가 필요할 때

**참고 파일**:
- `feature/pay/card/src/.../CardViewModel.kt`

---

### 패턴 E: onStart + collect 수동 수집 패턴

**lazy 초기화 패턴**. `MutableStateFlow`를 수동으로 업데이트합니다.

```kotlin
private val _uiState = MutableStateFlow<SettingState>(SettingState.Loading)
val uiState: StateFlow<SettingState> get() = _uiState

// 방법 1: onStart로 lazy 초기화
val uiState: StateFlow<SplashState> = _uiState.onStart {
	initState()
}.stateIn(
	scope = viewModelScope,
	started = SharingStarted.Lazily,
	initialValue = SplashState.Loading,
)

// 방법 2: init에서 수동 수집
init {
	observeData()
}

private fun observeData() {
	viewModelScope.launch {
		combine(
			repo1.observe(),
			repo2.observe(),
		) { data1, data2 ->
			UiState.Data(data1, data2)
		}.collect { data ->
			_uiState.update { data }
		}
	}
}
```

**사용 시점**: 복잡한 초기화 로직이 필요하거나 여러 독립적 Flow를 관리할 때

**참고 파일**:
- `feature/splash/src/.../SplashViewModel.kt`
- `feature/setting/src/.../SettingViewModel.kt`

---

## 페이지네이션 패턴

페이지네이션이 필요한 경우 **누적 리스트 패턴**을 사용합니다.

```kotlin
private val _filterState = MutableStateFlow(FilterState(offset = 0))
private val accumulativeListState = MutableStateFlow<List<Item>?>(null)

val uiState: TriggerStateFlow<ListUiState> = _filterState
	.flatMapLatest { filter ->
		// 필터 변경(페이지네이션 아닌 경우) → 누적 리스트 초기화
		if (!filter.isPaginated) {
			accumulativeListState.value = null
		}

		repository.getList(
			offset = filter.offset,
			onError = ::showMessage,
		)
	}.map { newItems ->
		val accumulated = accumulativeListState.value
		val mergedList = if (_filterState.value.isPaginated && accumulated != null) {
			accumulated + newItems
		} else {
			newItems
		}
		accumulativeListState.value = mergedList

		if (mergedList.isEmpty()) {
			ListUiState.Empty
		} else {
			ListUiState.Data(
				items = mergedList,
				isPageEnd = newItems.isEmpty(),
			)
		}
	}.triggerStateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(),
		initialValue = ListUiState.Loading,
	)

fun nextPage() {
	uiState.withData<ListUiState.Data> { state ->
		if (state.isPageEnd) return
		_filterState.update { it.copy(offset = it.offset + PAGE_SIZE, isPaginated = true) }
	}
}
```

**핵심**:
- `accumulativeListState`에 누적 저장
- 필터 변경 시 누적 초기화
- 페이지네이션 시 기존 리스트에 append

**참고 파일**:
- `feature/dispatch/list/src/.../DispatchListViewModel.kt`
- `feature/notice/src/.../NoticeViewModel.kt`
- `feature/cargo/list/src/.../CargoListViewModel.kt`

---

## TriggerStateFlow vs stateIn 선택 기준

| 조건 | 선택 | 이유 |
|------|------|------|
| 데이터 갱신(mutation) 후 재조회 필요 | `triggerStateIn` | `restart()`로 Flow 재시작 |
| Pull-to-Refresh 필요 | `triggerStateIn` | RefreshDelegate와 연동 |
| 읽기 전용 (수정 없음) | `stateIn` | restart 불필요 |
| 수동 트리거 사용 중 | `stateIn` | SharedFlow가 트리거 역할 |

---

## SharingStarted 전략 선택

| 전략 | 사용 시점 |
|------|----------|
| `WhileSubscribed(5000)` | 기본값. 대부분의 목록/상세 화면 |
| `WhileSubscribed(3000)` | 빠른 해제가 필요한 경우 |
| `WhileSubscribed()` | timeout 없이 구독 해제 즉시 중단 |
| `Lazily` | 첫 구독자 등장 시 시작, 이후 유지 |
| `Eagerly` | 필터 상태 등 즉시 공유 필요 시 |

---

## 상태 관리 규칙

### 1. 상태 타입 체크: withData / updateWithData

```kotlin
// ✅ 읽기 전용
uiState.withData<MyUiState.Data> { state ->
	doSomething(state.items)
}

// ✅ 상태 업데이트
_uiState.updateWithData<MyUiState, MyUiState.Data> { state ->
	state.copy(isLoading = true)
}

// ❌ 수동 is 체크 금지
val current = _uiState.value
if (current !is MyUiState.Data) return
```

### 2. MutableStateFlow 상태 변경: .update { }

```kotlin
// ✅ update 사용 (thread-safe)
_modalEffect.update { MyModalEffect.Hidden }
_filterState.update { it.copy(offset = 0) }

// ❌ .value = 금지
_modalEffect.value = MyModalEffect.Hidden
```

### 3. Flow 수집: collect 패턴

```kotlin
// ✅ collect
viewModelScope.launch {
	someFlow.collect { value -> handleValue(value) }
}

// ❌ launchIn 금지
someFlow.onEach { handleValue(it) }.launchIn(viewModelScope)
```

### 4. 함수 네이밍: on 접두어 없음

```kotlin
// ✅ ViewModel 함수
fun selectTab(tab: Tab) { }
fun search(query: String) { }
fun deleteItem(id: String) { }

// ❌ on 접두어 금지 (onCleared 등 프레임워크 override 제외)
fun onTabSelected(tab: Tab) { }
```

---

## Mutation 후 재조회 패턴

데이터 변경 후 UI를 갱신하는 두 가지 방법:

### 방법 1: TriggerStateFlow.restart()
```kotlin
fun deleteItem(id: String) {
	viewModelScope.launch {
		repository.deleteItem(id, onError = ::showMessage)
		uiState.restart()  // Flow 파이프라인 재실행
	}
}
```

### 방법 2: SharedFlow trigger 재발행
```kotlin
fun deleteItem(id: String) {
	viewModelScope.launch {
		repository.deleteItem(id, onError = ::showMessage)
		trigger.emit(Unit)  // 트리거 재발행
	}
}
```

---

## 패턴 선택 가이드

```
새 ViewModel 구현 시:

1. 데이터 소스가 1개인가?
   → 패턴 C (repository → map → triggerStateIn)

2. 데이터 소스가 2개 이상이고, 필터/검색이 없는가?
   → 패턴 B (combine → triggerStateIn)

3. 필터/검색 조건이 있는가?
   → 패턴 A (filter → flatMapLatest → combine → triggerStateIn)

4. 페이지네이션이 필요한가?
   → 패턴 A + 누적 리스트 패턴

5. Pull-to-Refresh가 필요한가?
   → RefreshableViewModel + createRefreshDelegate 추가

6. mutation 후 재조회가 필요한가?
   → triggerStateIn 사용 + restart()
```

---

## 참고 파일

- **FlowUtil**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/flow/FlowUtil.kt`
- **TriggerStateFlow**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/trigger/TriggerStateFlow.kt`
- **triggerStateIn**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/trigger/TriggerStateIn.kt`
- **RefreshableViewModel**: `core/utils/src/main/kotlin/ktc/cargo/driver/utils/viewmodel/RefreshableViewModel.kt`
- **ViewModel 템플릿**: `featureTemplate/feature/src/.../{{className}}ViewModel.kt`

## 체크리스트

- [ ] `@HiltViewModel internal class`인지
- [ ] ViewModel 함수에 `on` 접두어가 없는지
- [ ] 적합한 파생 Flow 패턴을 선택했는지 (A/B/C/D/E)
- [ ] `triggerStateIn` vs `stateIn` 선택이 올바른지
- [ ] `SharingStarted` 전략이 적합한지
- [ ] `withData` / `updateWithData` 유틸 사용하는지 (수동 `is` 체크 금지)
- [ ] MutableStateFlow 상태 변경에 `.update { }` 사용하는지 (`.value =` 금지)
- [ ] Flow 수집에 `collect` 패턴 사용하는지 (`launchIn` 금지)
- [ ] 페이지네이션 시 누적 리스트 패턴을 사용하는지
- [ ] mutation 후 재조회 로직이 있는지 (`restart()` 또는 trigger 재발행)
- [ ] RefreshableViewModel 필요 시 구현했는지
