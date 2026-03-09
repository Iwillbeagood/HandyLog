---
description: "새 Feature를 Navigation 시스템에 연결 - Route 정의, NavigateActionInterop, MainNavigator, MainNavHost, build.gradle 의존성 5곳 수정"
model: claude-haiku-4-5-20251001
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Navigation 연결 Skill

$ARGUMENTS 를 기반으로 새 Feature를 Navigation 시스템에 연결합니다.

## 전제 조건

Feature 모듈이 이미 구현되어 있어야 합니다:
- `feature/{name}/` 디렉토리 존재
- `{Name}Route.kt` Composable 존재
- `{Name}Navigation.kt` (NavController, NavGraphBuilder 확장 함수) 존재

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Notice`, `Block`, `CargoReturn`)
- **Route 타입**: `data object` (인자 없음) 또는 `data class` (인자 있음)
- **상위 Route 그룹**: 독립 Route 또는 기존 sealed interface 하위 (예: `Account.Block`, `Pay.Fee`)

## 수정 대상 파일 (5곳)

새 Feature 추가 시 **반드시 5개 파일을 수정**해야 합니다:

| 순서 | 파일 | 작업 |
|------|------|------|
| 1 | `core/navigation/.../Route.kt` | Route 정의 추가 |
| 2 | `core/navigation/.../NavigateActionInterop.kt` | 네비게이션 메서드 추가 |
| 3 | `feature/main/.../MainNavigator.kt` | Interop 구현 추가 |
| 4 | `feature/main/.../MainNavHost.kt` | NavGraph 등록 |
| 5 | `feature/main/build.gradle.kts` | Feature 모듈 의존성 추가 |

추가로 `settings.gradle.kts`에 새 모듈이 등록되어 있는지 확인합니다.

## 구현 순서

### 1단계: Route 정의 추가

**위치**: `core/navigation/src/main/kotlin/ktc/cargo/driver/navigation/Route.kt`

기존 `sealed interface Route` 에 새 Route를 추가합니다.

**패턴 A - 독립 Route (인자 없음)**:
```kotlin
sealed interface Route {
	// ... 기존 Route들 ...

	@Serializable
	data object NewFeature : Route
}
```

**패턴 B - 독립 Route (인자 있음)**:
```kotlin
sealed interface Route {
	// ... 기존 Route들 ...

	@Serializable
	data class NewFeature(
		val someId: String,
		val someData: Int,
	) : Route
}
```

**패턴 C - 그룹 하위 Route (sealed interface)**:
```kotlin
sealed interface Route {
	// ... 기존 Route들 ...

	@Serializable
	sealed interface NewGroup : Route {

		@Serializable
		data object List : NewGroup

		@Serializable
		data class Detail(
			val itemId: String,
		) : NewGroup
	}
}
```

**주의사항**:
- `@Serializable` 어노테이션 필수
- 인자에 Domain Model을 사용하는 경우, 해당 Model에 `@Serializable` 필요
- 복잡한 타입(List, 커스텀 객체)은 `NavTypeMap`에 TypeMap 등록 필요

**참고**: 기존 Route 구조를 `core/navigation/src/main/kotlin/ktc/cargo/driver/navigation/Route.kt`에서 먼저 확인하세요.

### 2단계: NavigateActionInterop에 메서드 추가

**위치**: `core/navigation/src/main/kotlin/ktc/cargo/driver/navigation/interop/NavigateActionInterop.kt`

`interface NavigateActionInterop`에 새 메서드를 추가합니다.

**패턴 A - 인자 없음**:
```kotlin
interface NavigateActionInterop {
	// ... 기존 메서드들 ...

	fun navigateToNewFeature()
}
```

**패턴 B - 인자 있음**:
```kotlin
interface NavigateActionInterop {
	// ... 기존 메서드들 ...

	fun navigateToNewFeature(
		someId: String,
		someData: Int,
	)
}
```

**주의**: import가 필요한 Domain Model이 있으면 파일 상단에 추가.

### 3단계: MainNavigator에 구현 추가

**위치**: `feature/main/src/main/kotlin/ktc/cargo/driver/main/navigation/MainNavigator.kt`

`navigationInteropImpl()` 메서드의 `object : NavigateActionInterop` 블록 안에 구현을 추가합니다.

**패턴 A - 인자 없음**:
```kotlin
override fun navigateToNewFeature() {
	navController.navigateToNewFeature()
}
```

**패턴 B - 인자 있음**:
```kotlin
override fun navigateToNewFeature(
	someId: String,
	someData: Int,
) {
	navController.navigateToNewFeature(
		someId = someId,
		someData = someData,
	)
}
```

**주의**:
- Feature 모듈의 Navigation 확장 함수 import 추가 필요
- 예: `import ktc.cargo.driver.newFeature.navigation.navigateToNewFeature`

### 4단계: MainNavHost에 NavGraph 등록

**위치**: `feature/main/src/main/kotlin/ktc/cargo/driver/main/navigation/MainNavHost.kt`

`NavGraphBuilder.() -> Unit` 블록 안에 새 navGraph 호출을 추가합니다.

```kotlin
val builder: NavGraphBuilder.() -> Unit = remember(navigator.navController) {
	{
		// ... 기존 navGraph들 ...
		newFeatureNavGraph()
	}
}
```

**주의**:
- Feature 모듈의 navGraph 함수 import 추가 필요
- 예: `import ktc.cargo.driver.newFeature.navigation.newFeatureNavGraph`

### 5단계: feature:main build.gradle.kts에 의존성 추가

**위치**: `feature/main/build.gradle.kts`

`dependencies` 블록에 새 feature 모듈 추가:

```kotlin
dependencies {
	// ... 기존 의존성들 ...
	implementation(projects.feature.newFeature)
}
```

**네이밍 규칙**:
- 모듈명이 `feature:notice`면 → `projects.feature.notice`
- 모듈명이 `feature:cargo-setting`면 → `projects.feature.cargoSetting` (kebab → camelCase)
- 모듈명이 `feature:account:block`면 → `projects.feature.account.block` (하위 모듈)

### 6단계: settings.gradle.kts 확인

**위치**: `settings.gradle.kts`

feature 섹션에 새 모듈이 등록되어 있는지 확인합니다. 없으면 추가:

```kotlin
// feature
include(
    // ... 기존 모듈들 ...
    ":feature:new-feature",
)
```

## Feature Navigation 파일 패턴 (참고)

Feature 모듈의 Navigation 파일이 아래 패턴을 따르는지 확인합니다.

**위치**: `feature/{name}/src/main/kotlin/ktc/cargo/driver/{name}/navigation/{Name}Navigation.kt`

**패턴 A - 인자 없음**:
```kotlin
package ktc.cargo.driver.newFeature.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ktc.cargo.driver.navigation.Route
import ktc.cargo.driver.newFeature.NewFeatureRoute

fun NavController.navigateToNewFeature() {
	navigate(Route.NewFeature)
}

fun NavGraphBuilder.newFeatureNavGraph() {
	composable<Route.NewFeature> {
		NewFeatureRoute()
	}
}
```

**패턴 B - 인자 있음**:
```kotlin
fun NavController.navigateToNewFeature(
	someId: String,
	someData: Int,
) {
	navigate(Route.NewFeature(someId = someId, someData = someData))
}

fun NavGraphBuilder.newFeatureNavGraph() {
	composable<Route.NewFeature> { backStackEntry ->
		NewFeatureRoute()
	}
}
```

## 실제 파일 경로 요약

```
core/navigation/src/main/kotlin/ktc/cargo/driver/navigation/
├── Route.kt                              ← 1. Route 정의
└── interop/
    └── NavigateActionInterop.kt          ← 2. 인터페이스 메서드

feature/main/src/main/kotlin/ktc/cargo/driver/main/navigation/
├── MainNavigator.kt                      ← 3. 네비게이션 구현
└── MainNavHost.kt                        ← 4. NavGraph 등록

feature/main/build.gradle.kts             ← 5. 모듈 의존성
settings.gradle.kts                       ← 6. 모듈 등록 확인
```

## 체크리스트

구현 완료 후 확인:
- [ ] `Route.kt`에 `@Serializable` Route 추가했는지
- [ ] `NavigateActionInterop.kt`에 `navigateTo{Feature}()` 메서드 추가했는지
- [ ] `MainNavigator.kt`의 `navigationInteropImpl()` 에 override 구현 추가했는지
- [ ] `MainNavigator.kt`에 feature navigation import 추가했는지
- [ ] `MainNavHost.kt`에 `{feature}NavGraph()` 호출 추가했는지
- [ ] `MainNavHost.kt`에 feature navigation import 추가했는지
- [ ] `feature/main/build.gradle.kts`에 `implementation(projects.feature.{name})` 추가했는지
- [ ] `settings.gradle.kts`에 `:feature:{name}` 모듈이 등록되어 있는지
