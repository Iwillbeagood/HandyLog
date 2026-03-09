---
description: "Figma 링크에서 디자인을 분석하여 Compose UI 코드로 변환하고, 디자인 시스템 네이밍 불일치 시 리포트 생성"
model: claude-opus-4-6
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - mcp__figma-remote-mcp__get_screenshot
  - mcp__figma-remote-mcp__get_design_context
  - mcp__figma-remote-mcp__get_variable_defs
  - mcp__figma-remote-mcp__get_metadata
---

# Figma → Compose UI Skill

$ARGUMENTS 에서 Figma 링크를 받아 디자인을 분석하고, 프로젝트 디자인 시스템에 맞는 Compose UI 코드를 생성합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **Figma URL**: `https://figma.com/design/:fileKey/:fileName?node-id=:nodeId` 형식
- **기능명** (선택): 생성할 Screen/Component의 이름 (없으면 Figma 노드명에서 추론)
- **대상 파일 경로** (선택): 코드를 작성할 위치 (없으면 기능명 기반으로 결정)

인자가 불충분한 경우 사용자에게 질문합니다.

---

## 실행 순서

### 1단계: Figma 디자인 분석

Figma URL에서 `fileKey`와 `nodeId`를 추출합니다.
- URL 형식: `https://figma.com/design/:fileKey/:fileName?node-id=:int1-:int2`
- `fileKey` = URL의 `:fileKey` 부분
- `nodeId` = `node-id` 파라미터 값 (예: `1-2` → `1:2`로 변환)

**순서대로 Figma MCP 도구를 호출합니다:**

#### 1-1. 스크린샷 확인
`get_screenshot`으로 디자인의 시각적 구조를 파악합니다.

```
도구: mcp__figma-remote-mcp__get_screenshot
파라미터: fileKey, nodeId
```

#### 1-2. 디자인 컨텍스트 가져오기
`get_design_context`로 UI 코드 힌트와 구조를 가져옵니다.

```
도구: mcp__figma-remote-mcp__get_design_context
파라미터: fileKey, nodeId, clientLanguages="kotlin", clientFrameworks="jetpack compose"
```

#### 1-3. 변수 정의 가져오기
`get_variable_defs`로 Figma에서 사용된 디자인 토큰(색상, 타이포 등)을 가져옵니다.

```
도구: mcp__figma-remote-mcp__get_variable_defs
파라미터: fileKey, nodeId
```

#### 1-4. 메타데이터 확인 (필요시)
구조가 복잡한 경우 `get_metadata`로 노드 계층 구조를 추가 확인합니다.

```
도구: mcp__figma-remote-mcp__get_metadata
파라미터: fileKey, nodeId
```

---

### 2단계: 디자인 시스템 매핑

Figma에서 가져온 디자인 토큰을 프로젝트 디자인 시스템과 매핑합니다.

#### 색상 매핑

Figma의 변수명을 `KTCTheme.colorScheme`에 매핑합니다:

| Figma 변수명 패턴 | 프로젝트 매핑 |
|-------------------|-------------|
| `text/primary` | `KTCTheme.colorScheme.text.primary` |
| `text/secondary` | `KTCTheme.colorScheme.text.secondary` |
| `text/third` | `KTCTheme.colorScheme.text.third` |
| `text/disable` | `KTCTheme.colorScheme.text.disable` |
| `text/white` | `KTCTheme.colorScheme.text.white` |
| `inputField/text` | `KTCTheme.colorScheme.inputField.text` |
| `inputField/hint` | `KTCTheme.colorScheme.inputField.hint` |
| `inputField/border` | `KTCTheme.colorScheme.inputField.border` |
| `inputField/container` | `KTCTheme.colorScheme.inputField.container` |
| `inputField/fixed` | `KTCTheme.colorScheme.inputField.fixed` |
| `tab/text` | `KTCTheme.colorScheme.tab.text` |
| `tab/textSelected` | `KTCTheme.colorScheme.tab.textSelected` |
| `tab/container` | `KTCTheme.colorScheme.tab.container` |
| `tab/containerSelected` | `KTCTheme.colorScheme.tab.containerSelected` |
| `icon/default` | `KTCTheme.colorScheme.icon.default` |
| `icon/inverse` | `KTCTheme.colorScheme.icon.inverse` |
| `button/text` | `KTCTheme.colorScheme.button.text` |
| `button/textDisable` | `KTCTheme.colorScheme.button.textDisable` |
| `button/containerDisable` | `KTCTheme.colorScheme.button.containerDisable` |
| `line/topbar` | `KTCTheme.colorScheme.line.topbar` |
| `line/divider1px` | `KTCTheme.colorScheme.line.divider1px` |
| `line/divider10px` | `KTCTheme.colorScheme.line.divider10px` |
| `line/border` | `KTCTheme.colorScheme.line.border` |
| `point/blue` | `KTCTheme.colorScheme.point.pointBlue` |
| `point/red` | `KTCTheme.colorScheme.point.pointRed` |
| `point/navy` | `KTCTheme.colorScheme.point.pointNavy` |
| `point/green` | `KTCTheme.colorScheme.point.pointGreen` |
| `surface` | `KTCTheme.colorScheme.surface` |
| `container` | `KTCTheme.colorScheme.container` |
| `error` | `KTCTheme.colorScheme.error` |
| `main` | `KTCTheme.colorScheme.main` |

**개별 색상 매핑** (Figma에서 직접 색상값이 나온 경우):

| Figma 색상값 | 프로젝트 매핑 |
|-------------|-------------|
| `#2B2F44` | `KTCTheme.colorScheme.main` |
| `#262626` | `KTCTheme.colorScheme.black800` |
| `#555555` | `KTCTheme.colorScheme.black600` |
| `#7B7B7B` | `KTCTheme.colorScheme.black500` |
| `#9D9D9D` | `KTCTheme.colorScheme.black400` |
| `#C4C4C4` | `KTCTheme.colorScheme.black300` |
| `#E9E9E9` | `KTCTheme.colorScheme.line.divider1px` 또는 `black100` |
| `#F5F5F5` | `KTCTheme.colorScheme.line.divider10px` 또는 `black50` |
| `#F2F3F6` | `KTCTheme.colorScheme.black30` |
| `#FFFFFF` | `KTCTheme.colorScheme.white` |
| `#2260FF` | `KTCTheme.colorScheme.blue1` |
| `#EC4352` | `KTCTheme.colorScheme.red1` 또는 `error` |
| `#47C73C` | `KTCTheme.colorScheme.green1` |

> **매핑 불가능한 색상**: Figma 변수명이나 색상값이 위 매핑 테이블에 없으면 **3단계 불일치 리포트**에 기록하고, 코드에서는 임시 하드코딩합니다:
> ```kotlin
> // TODO: 새로운 색상 필요 - KTCColorScheme에 추가 요청
> // Figma 변수명: {Figma에서 사용된 변수명}
> // 용도: {사용 목적}
> // 제안 색상: Color(0xFF{hex값})
> ```

#### 타이포그래피 매핑

Figma의 텍스트 스타일을 `KTCTheme.typography`에 매핑합니다:

| Figma 텍스트 속성 | 프로젝트 매핑 |
|------------------|-------------|
| Bold / 30sp | `KTCTheme.typography.bold30` |
| Bold / 26sp | `KTCTheme.typography.bold26` |
| Bold / 24sp | `KTCTheme.typography.bold24` |
| Bold / 22sp | `KTCTheme.typography.bold22` |
| Bold / 20sp | `KTCTheme.typography.bold20` |
| Bold / 18sp | `KTCTheme.typography.bold18` |
| Bold / 16sp | `KTCTheme.typography.bold16` |
| Bold / 14sp | `KTCTheme.typography.bold14` |
| Bold / 12sp | `KTCTheme.typography.bold12` |
| Bold / 10sp | `KTCTheme.typography.bold10` |
| Medium / 24sp | `KTCTheme.typography.medium24` |
| Medium / 22sp | `KTCTheme.typography.medium22` |
| Medium / 20sp | `KTCTheme.typography.medium20` |
| Medium / 18sp | `KTCTheme.typography.medium18` |
| Medium / 16sp | `KTCTheme.typography.medium16` |
| Medium / 14sp | `KTCTheme.typography.medium14` |
| Medium / 12sp | `KTCTheme.typography.medium12` |
| Medium / 10sp | `KTCTheme.typography.medium10` |
| Regular / 24sp | `KTCTheme.typography.regular24` |
| Regular / 22sp | `KTCTheme.typography.regular22` |
| Regular / 20sp | `KTCTheme.typography.regular20` |
| Regular / 18sp | `KTCTheme.typography.regular18` |
| Regular / 16sp | `KTCTheme.typography.regular16` |
| Regular / 14sp | `KTCTheme.typography.regular14` |
| Regular / 12sp | `KTCTheme.typography.regular12` |
| Regular / 10sp | `KTCTheme.typography.regular10` |
| Poppins Bold / 26sp | `KTCTheme.typography.poppinsBold26` |
| Poppins SemiBold / 18sp | `KTCTheme.typography.poppinsSemiBold18` |
| Poppins Medium / 16sp | `KTCTheme.typography.poppinsMedium16` |

#### 컴포넌트 매핑

Figma 컴포넌트를 프로젝트 디자인 시스템 컴포넌트에 매핑합니다:

| Figma 컴포넌트 패턴 | 프로젝트 컴포넌트 |
|-------------------|----------------|
| 전체 너비 하단 고정 버튼 | `LargeButton` |
| 둥근 모서리 일반 버튼 | `RegularButton` |
| 테두리만 있는 버튼 | `BorderButton` |
| 선택 가능한 버튼 | `SelectorButton` |
| 텍스트 입력 필드 | `HmInputField` |
| 체크박스 | `HmCheckBox` |
| 라디오 버튼 | `HmRadio` |
| 토글 스위치 | `HmSwitch` |
| 상단 네비게이션 바 | `HmTopAppbar` |
| 기본 화면 레이아웃 | `HmNormalScaffold` 또는 `HmDefaultScaffold` |
| Pull-to-Refresh 레이아웃 | `HmRefreshScaffold` |
| 다이얼로그 (확인/취소) | `DefaultDialog` |
| 다이얼로그 (확인만) | `ConfirmDialog` |
| 커스텀 다이얼로그 | `BaseDialog` |
| 바텀 시트 | `BaseBottomSheet` 또는 `DefaultBottomSheet` |
| 필터 칩 목록 | `HmFilter` + `FilterBuilder` |
| 채워진 라벨/뱃지 | `HmLabel` |
| 테두리 라벨/뱃지 | `HmBorderLabel` |
| 탭 바 | `HmTabs` |
| 로딩 인디케이터 | `HmLoading` |
| 1px 구분선 | `HmHorizontalDivider` |
| 10px 구분선 (영역 분리) | `HmHorizontalDivider` (thickness = 10.dp, color = line.divider10px) |
| 세로 간격 | `HmVerticalSpacer` |
| 가로 간격 | `HmHorizontalSpacer` |
| 드롭다운 | `HmDropDown` |
| 플로팅 버튼 | `HmFloatingButton` |

> **매핑 불가능한 컴포넌트**: 디자인 시스템에 없는 UI 요소는 커스텀으로 직접 구현하되, 색상/타이포는 반드시 `KTCTheme`을 사용합니다.

---

### 3단계: 네이밍 불일치 리포트 생성

Figma 변수와 프로젝트 디자인 시스템 간 매핑이 안 되는 항목이 있으면, 프로젝트 루트에 리포트 파일을 생성합니다.

**파일 위치**: `FIGMA_DESIGN_MISMATCH.md`

**형식**:
```markdown
# Figma ↔ 프로젝트 디자인 시스템 불일치 리포트

> 생성일: {날짜}
> Figma URL: {원본 URL}
> 노드: {nodeId}

## 색상 불일치

| Figma 변수명 | Figma 값 | 프로젝트 매핑 | 상태 |
|-------------|---------|-------------|------|
| `{변수명}` | `#{hex}` | 매핑 없음 | 추가 필요 |
| `{변수명}` | `#{hex}` | `KTCTheme.colorScheme.{속성}` 과 유사하나 값 다름 | 확인 필요 |

## 타이포그래피 불일치

| Figma 스타일 | Figma 값 | 프로젝트 매핑 | 상태 |
|-------------|---------|-------------|------|
| `{스타일명}` | `{weight} / {size}sp` | 매핑 없음 | 추가 필요 |

## 컴포넌트 불일치

| Figma 컴포넌트 | 설명 | 프로젝트 매핑 | 상태 |
|---------------|------|-------------|------|
| `{컴포넌트명}` | {설명} | 커스텀 구현 필요 | 추가 필요 |

## 조치 사항

- [ ] 위 불일치 항목을 디자인팀과 확인
- [ ] 필요한 색상을 `KTCColorScheme`에 추가
- [ ] 필요한 타이포를 `KTCTypography`에 추가
- [ ] 필요한 컴포넌트를 `core/designsystem/component/`에 추가
```

**규칙**:
- 불일치가 **없으면** 리포트 파일을 생성하지 않습니다.
- 기존 `FIGMA_DESIGN_MISMATCH.md` 파일이 있으면 **기존 내용에 새 섹션을 추가**합니다 (덮어쓰지 않음).

---

### 4단계: Compose UI 코드 생성

분석된 디자인을 기반으로 Composable 코드를 작성합니다.

#### 코드 작성 규칙

**색상**: `KTCTheme.colorScheme`만 사용
```kotlin
// ✅ 올바름
color = KTCTheme.colorScheme.text.primary
modifier = Modifier.background(KTCTheme.colorScheme.surface)

// ❌ 금지
color = MaterialTheme.colorScheme.onSurface
color = Color(0xFF262626)
color = HmmColor.Black800
```

**타이포**: `KTCTheme.typography`만 사용
```kotlin
// ✅ 올바름
style = KTCTheme.typography.bold20

// ❌ 금지
fontSize = 20.sp, fontWeight = FontWeight.Bold
```

**컴포넌트**: 디자인 시스템 컴포넌트 우선 사용
```kotlin
// ✅ 디자인 시스템 컴포넌트 사용
HmNormalScaffold(title = "제목", onBackClick = onGoBack) { ... }
LargeButton(text = "확인", onClick = onConfirm)
HmInputField(value = text, onValueChange = onTextChange, hint = "입력")

// ❌ 직접 구현하지 않음 (디자인 시스템에 있는 경우)
Scaffold(topBar = { TopAppBar(...) }) { ... }
Button(onClick = onConfirm) { Text("확인") }
```

**새 색상이 필요한 경우**:
```kotlin
// TODO: 새로운 색상 필요 - KTCColorScheme에 추가 요청
// Figma 변수명: accent/warning
// 용도: 경고 배너 배경색
// 제안 색상: Color(0xFFFFF3CD)
Box(modifier = Modifier.background(Color(0xFFFFF3CD)))
```

**클릭 영역**: `RoundedCornerShape(4.dp)` Clip 처리
```kotlin
Box(
    modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .clickableSingle { onClick() }
)
```

**Preview 패턴**:
```kotlin
@ThemePreviews
@Composable
private fun MyScreenPreview() {
    HmmTheme {
        MyScreen(/* preview data */)
    }
}
```

#### 코드 구조

Screen 파일을 생성합니다. 위치는 사용자가 지정하거나, 기능명 기반으로 결정합니다:

```
feature/{feature-name}/src/main/kotlin/ktc/cargo/driver/{featureName}/{Name}Screen.kt
```

**기본 Screen 구조**:
```kotlin
package ktc.cargo.driver.{featureName}

import androidx.compose.runtime.Composable
import ktc.cargo.driver.designsystem.etc.ThemePreviews
import ktc.cargo.driver.designsystem.theme.HmmTheme
import ktc.cargo.driver.designsystem.theme.KTCTheme

@Composable
internal fun {Name}Screen(
    uiState: {Name}UiState.Data,
    // 콜백 파라미터 (on 접두어)
    onGoBack: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    // Figma 디자인을 반영한 UI 구현
}

@ThemePreviews
@Composable
private fun {Name}ScreenPreview() {
    HmmTheme {
        {Name}Screen(
            uiState = {Name}UiState.Data(/* preview data */),
        )
    }
}
```

---

## 주의사항

1. **Figma 디자인을 최대한 충실히 반영**하되, 프로젝트 디자인 시스템을 우선합니다.
2. **px → dp 변환**: Figma의 px 값은 Android의 dp와 동일하게 취급합니다.
3. **Figma 변수명과 프로젝트 토큰이 일치하면** 해당 토큰을 그대로 사용합니다.
4. **일치하지 않는 경우** 가장 가까운 토큰을 사용하고, 불일치를 리포트에 기록합니다.
5. **레이아웃 구조**는 Figma의 Auto Layout 설정을 참고하여 `Row`, `Column`, `Box`로 변환합니다.
6. **Figma의 padding/spacing**은 `Modifier.padding()`과 `HmVerticalSpacer`/`HmHorizontalSpacer`로 변환합니다.

---

## 체크리스트

코드 생성 완료 후 확인:
- [ ] 모든 색상이 `KTCTheme.colorScheme`을 사용하는지
- [ ] 모든 타이포가 `KTCTheme.typography`를 사용하는지
- [ ] `MaterialTheme.colorScheme` 사용이 없는지
- [ ] `HmmColor` 직접 사용이 없는지
- [ ] 하드코딩 색상에 TODO 주석이 있는지
- [ ] 디자인 시스템 컴포넌트를 우선 사용했는지
- [ ] 클릭 영역에 `RoundedCornerShape(4.dp)` Clip 처리했는지
- [ ] `@ThemePreviews` + `HmmTheme { }` Preview가 있는지
- [ ] Figma와 프로젝트 간 불일치가 있으면 `FIGMA_DESIGN_MISMATCH.md`에 기록했는지
- [ ] 불일치가 없으면 리포트 파일을 생성하지 않았는지
