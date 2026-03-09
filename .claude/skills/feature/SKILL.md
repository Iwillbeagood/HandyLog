---
description: "Feature 모듈의 Presentation Layer만 구현 - UiState, Effect, ViewModel, Route, Screen, Navigation 파일 생성 (Domain/Data는 이미 존재할 때)"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Feature (Presentation) Layer Skill

$ARGUMENTS 를 기반으로 Feature 모듈을 구현합니다.

## 전제 조건

Domain Layer와 Data Layer가 먼저 구현되어 있어야 합니다:
- Domain Model, Repository Interface, UseCase
- RepositoryImpl, DataSource, Hilt 모듈

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Notice`, `CargoSetting`, `Block`)
- **UI 요구사항**: 화면 구성, 사용자 인터랙션

## 모듈 생성

### build.gradle.kts

**위치**: `feature/{feature-name}/build.gradle.kts`

```kotlin
import ktc.cargo.driver.setNamespace

plugins {
	alias(libs.plugins.hmm.android.feature)
}

android {
	setNamespace("{featureName}")
}

dependencies {
	// 필요한 core 모듈이나 다른 feature 모듈만 추가
	// implementation(projects.core.kakao)
	// implementation(projects.core.device)
}
```

> **참고**: `hmm-android-feature` 플러그인이 아래 의존성을 자동 포함하므로 별도 추가 불필요:
> - `:core:ui`, `:domain:usecase`, `:domain:repository`, `:domain:model`
> - `:core:testing` (testImplementation)
> - Lifecycle, Navigation, Serialization, Immutable Collections 등
>
> `dependencies`에는 기능별로 필요한 **core 모듈** (예: `core.kakao`, `core.device`)이나 **다른 feature 모듈**만 추가합니다.

**참고 파일**:
- `feature/notice/build.gradle.kts`
- `feature/login/build.gradle.kts`
- `feature/setting/build.gradle.kts`

## 구현 순서 (6단계)

### 1단계: Contract - State 정의

**위치**: `feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/contract/{ClassName}State.kt`

**규칙**:
- `internal sealed interface` + `@Stable`
- 각 상태는 `@Immutable`
- Loading, Data 상태 필수
- 패키지: `ktc.cargo.driver.{featureName}.contract`

**참고 패턴**:
```kotlin
package ktc.cargo.driver.notice.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ktc.cargo.driver.domain.model.notice.Notice

@Stable
internal sealed interface NoticeUiState {

	@Immutable
	data object Loading : NoticeUiState

	@Immutable
	data object Empty : NoticeUiState

	@Immutable
	data class Data(
		val notices: List<Notice>,
		val isPageEnd: Boolean,
	) : NoticeUiState
}
```

**참고 파일**:
- `feature/notice/src/main/kotlin/ktc/cargo/driver/notice/contract/NoticeUiState.kt`
- `featureTemplate/feature/src/main/kotlin/ktc/cargo/driver/{{lowerCamelClassName}}/contract/{{className}}State.kt`

### 2단계: Contract - Effect 정의

**위치**: `feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/contract/{ClassName}Effect.kt`

**규칙**:
- `@Stable internal sealed interface`
- Effect: 일회성 이벤트 (Snackbar, Navigation 등) - `SharedFlow`로 관리
- ModalEffect: 다이얼로그/바텀시트 상태 - `StateFlow`로 관리
- Hidden이 기본 상태

**참고 패턴**:
```kotlin
package ktc.cargo.driver.notice.contract

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ktc.cargo.driver.domain.model.MessageType

@Stable
internal sealed interface NoticeModalEffect {

	@Immutable
	data object Hidden : NoticeModalEffect
}

@Stable
internal sealed interface NoticeUiEffect {

	@Immutable
	data class ShowMessage(val messageType: MessageType) : NoticeUiEffect
}
```

**참고 파일**:
- `featureTemplate/feature/src/main/kotlin/ktc/cargo/driver/{{lowerCamelClassName}}/contract/{{className}}Effect.kt`

### 3단계: ViewModel 구현

**위치**: `feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/{ClassName}ViewModel.kt`

**`/viewmodel` 스킬을 참조하여 구현합니다.** 파생 Flow 파이프라인 패턴(A~E), 페이지네이션, RefreshableViewModel, 상태 관리 규칙 등 모든 ViewModel 패턴은 해당 스킬에 정의되어 있습니다.

**참고 파일**:
- `feature/notice/src/main/kotlin/ktc/cargo/driver/notice/NoticeViewModel.kt`
- `featureTemplate/feature/src/main/kotlin/ktc/cargo/driver/{{lowerCamelClassName}}/{{className}}ViewModel.kt`

### 4단계: Route 구현 (State 수집 + Effect 핸들링)

**위치**: `feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/{ClassName}Route.kt`

**규칙**:
- Route: ViewModel 연결, State 수집, Effect 처리, Modal 분리
- Content: 상태에 따른 UI 분기 (`HmFadeAnimatedVisibility`)
- ModalContent: 모달 이펙트에 따른 다이얼로그/바텀시트 표시
- **콜백 함수에 `on` 접두어 사용** (예: `onGoBack`, `onTabSelected`)

**참고 패턴**:
```kotlin
package ktc.cargo.driver.notice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ktc.cargo.driver.designsystem.component.HmFadeAnimatedVisibility
import ktc.cargo.driver.designsystem.component.HmNormalScaffold
import ktc.cargo.driver.designsystem.theme.ChangeStatusBarColor
import ktc.cargo.driver.navigation.interop.LocalNavigateActionInterop
import ktc.cargo.driver.navigation.interop.rememberShowSnackBar
import ktc.cargo.driver.notice.contract.NoticeModalEffect
import ktc.cargo.driver.notice.contract.NoticeUiEffect
import ktc.cargo.driver.notice.contract.NoticeUiState

@Composable
internal fun NoticeRoute(
	viewModel: NoticeViewModel = hiltViewModel(),
) {
	ChangeStatusBarColor()

	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	val modalEffect by viewModel.modalEffect.collectAsStateWithLifecycle()
	val navAction = LocalNavigateActionInterop.current
	val showSnackbar = rememberShowSnackBar()

	NoticeContent(
		uiState = uiState,
		onGoBack = navAction::popBackStack,
	)

	NoticeModalContent(
		modalEffect = modalEffect,
		onDismissRequest = viewModel::dismiss,
	)

	LaunchedEffect(true) {
		viewModel.uiEffect.collect { effect ->
			when (effect) {
				is NoticeUiEffect.ShowMessage -> showSnackbar(effect.messageType)
			}
		}
	}
}

@Composable
private fun NoticeContent(
	uiState: NoticeUiState,
	onGoBack: () -> Unit,
) {
	HmNormalScaffold(
		title = "공지사항",
		onBackClick = onGoBack,
	) {
		HmFadeAnimatedVisibility(uiState is NoticeUiState.Data) {
			if (uiState is NoticeUiState.Data) {
				NoticeScreen(
					uiState = uiState,
				)
			}
		}
	}
}

@Composable
private fun NoticeModalContent(
	modalEffect: NoticeModalEffect,
	onDismissRequest: () -> Unit,
) {
	when (modalEffect) {
		NoticeModalEffect.Hidden -> {}
	}
}
```

**참고 파일**:
- `featureTemplate/feature/src/main/kotlin/ktc/cargo/driver/{{lowerCamelClassName}}/{{className}}Route.kt`

### 5단계: Screen 구현 (실제 UI)

**위치**: `feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/{ClassName}Screen.kt`

**규칙**:
- `internal fun` Composable
- State의 Data 타입을 파라미터로 받음
- **KTCTheme.colorScheme** 사용 (MaterialTheme.colorScheme 금지)
- **KTCTheme.typography** 사용
- 디자인 시스템 컴포넌트 활용 (HmNormalScaffold, HmTopAppbar, LargeButton 등)
- Preview: `@ThemePreviews` + `HmmTheme { }` 감싸기
- 클릭 영역: `RoundedCornerShape(4.dp)` Clip 처리

**참고 패턴**:
```kotlin
package ktc.cargo.driver.notice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ktc.cargo.driver.designsystem.etc.ThemePreviews
import ktc.cargo.driver.designsystem.theme.HmmTheme
import ktc.cargo.driver.designsystem.theme.KTCTheme
import ktc.cargo.driver.notice.contract.NoticeUiState

@Composable
internal fun NoticeScreen(
	uiState: NoticeUiState.Data,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 16.dp),
	) {
		// UI 구현
		Text(
			text = "공지사항 목록",
			style = KTCTheme.typography.bold20,
			color = KTCTheme.colorScheme.text.primary,
		)
	}
}

@ThemePreviews
@Composable
private fun NoticeScreenPreview() {
	HmmTheme {
		NoticeScreen(
			uiState = NoticeUiState.Data(
				notices = emptyList(),
				isPageEnd = false,
			),
		)
	}
}
```

**디자인 시스템 가이드**: `.github/prompts/design-system.prompt.md` 참조
**핵심 규칙**:
- 색상: `KTCTheme.colorScheme.text.primary`, `KTCTheme.colorScheme.surface` 등
- 타이포: `KTCTheme.typography.bold20`, `KTCTheme.typography.regular14` 등
- `MaterialTheme.colorScheme` 사용 절대 금지
- `HmmColor` 직접 사용 절대 금지
- 하드코딩 `Color(0xFF...)` 사용 금지 (새 색상 필요 시 TODO 주석 + 임시 하드코딩만 허용)

**참고 파일**:
- `feature/notice/src/main/kotlin/ktc/cargo/driver/notice/NoticeDetailScreen.kt`
- `featureTemplate/feature/src/main/kotlin/ktc/cargo/driver/{{lowerCamelClassName}}/{{className}}Screen.kt`
- `.github/prompts/design-system.prompt.md` (컴포넌트 상세 API)

### 6단계: Navigation 정의

**위치**: `feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/navigation/{ClassName}Navigation.kt`

**규칙**:
- `NavController.navigateTo{ClassName}()` 확장 함수
- `NavGraphBuilder.{lowerCamelClassName}NavGraph()` 확장 함수
- Route 정의는 `core/navigation/.../Route.kt`에 별도 추가 필요

**참고 패턴**:
```kotlin
package ktc.cargo.driver.notice.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ktc.cargo.driver.navigation.Route
import ktc.cargo.driver.notice.NoticeRoute

fun NavController.navigateToNotice() {
	navigate(Route.Notice)
}

fun NavGraphBuilder.noticeNavGraph() {
	composable<Route.Notice> {
		NoticeRoute()
	}
}
```

**참고 파일**:
- `featureTemplate/feature/src/main/kotlin/ktc/cargo/driver/{{lowerCamelClassName}}/navigation/{{className}}Navigation.kt`

## 추가 연결 작업 (Navigation 스킬과 연계)

Feature 모듈 생성 후 다음 파일들의 수정이 필요합니다:

1. **Route 등록**: `core/navigation/.../Route.kt`에 새 Route 추가
2. **NavigateActionInterop**: `navigateTo{Feature}()` 메서드 추가
3. **MainNavigator**: NavigateActionInterop 구현에 메서드 추가
4. **MainNavHost**: `{feature}NavGraph()` 등록
5. **feature:main build.gradle.kts**: 새 feature 모듈 의존성 추가

> 이 작업은 `/navigation` 스킬에서 처리합니다. 해당 스킬이 없는 경우 수동으로 위 파일들을 수정하세요.

## 함수 네이밍 규칙

```kotlin
// ✅ Composable 콜백: on 접두어
@Composable
fun MyScreen(
	onTabSelected: (Tab) -> Unit,
	onSearchClick: () -> Unit,
	onGoBack: () -> Unit,
)

// ✅ ViewModel 함수: on 없음
class MyViewModel {
	fun selectTab(tab: Tab) { }
	fun search() { }
}

// ✅ 연결
MyScreen(
	onTabSelected = viewModel::selectTab,
	onSearchClick = viewModel::search,
	onGoBack = navAction::popBackStack,
)
```

## 파일 위치 요약

| 단계 | 파일 | 위치 |
|------|------|------|
| build.gradle.kts | 모듈 설정 | `feature/{name}/build.gradle.kts` |
| 1. State | Contract | `feature/{name}/src/main/kotlin/ktc/cargo/driver/{name}/contract/{Name}State.kt` |
| 2. Effect | Contract | `feature/{name}/src/main/kotlin/ktc/cargo/driver/{name}/contract/{Name}Effect.kt` |
| 3. ViewModel | 로직 | `feature/{name}/src/main/kotlin/ktc/cargo/driver/{name}/{Name}ViewModel.kt` |
| 4. Route | 연결 | `feature/{name}/src/main/kotlin/ktc/cargo/driver/{name}/{Name}Route.kt` |
| 5. Screen | UI | `feature/{name}/src/main/kotlin/ktc/cargo/driver/{name}/{Name}Screen.kt` |
| 6. Navigation | 경로 | `feature/{name}/src/main/kotlin/ktc/cargo/driver/{name}/navigation/{Name}Navigation.kt` |

## 체크리스트

구현 완료 후 확인:
- [ ] State가 `@Stable sealed interface`인지
- [ ] 각 상태에 `@Immutable`이 있는지
- [ ] ViewModel이 `@HiltViewModel internal class`인지
- [ ] ViewModel 함수에 `on` 접두어가 없는지
- [ ] Composable 콜백에 `on` 접두어가 있는지
- [ ] `withData` / `updateWithData` 유틸 사용하는지 (수동 `is` 체크 금지)
- [ ] MutableStateFlow 상태 변경에 `.update { }` 사용하는지 (`.value =` 금지)
- [ ] Flow 수집에 `collect` 패턴 사용하는지 (`launchIn` 금지)
- [ ] `KTCTheme.colorScheme` 사용하는지 (`MaterialTheme.colorScheme` 금지)
- [ ] `KTCTheme.typography` 사용하는지
- [ ] Preview에 `@ThemePreviews` + `HmmTheme { }` 사용하는지
- [ ] `settings.gradle.kts`에 새 모듈이 등록되어 있는지
