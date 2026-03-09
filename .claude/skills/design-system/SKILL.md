---
description: "Compose UI 코드 작성 시 자동 참조되는 디자인 시스템 규칙 (KTCTheme 색상, 타이포, 컴포넌트 매핑)"
user-invocable: false
allowed-tools:
  - Read
  - Glob
  - Grep
---

# 디자인 시스템 핵심 규칙

UI Composable 코드를 작성할 때 이 규칙을 반드시 따릅니다.

## 색상 규칙

**유일한 정답**: `KTCTheme.colorScheme`을 통해서만 색상에 접근합니다.

```kotlin
// ✅ 올바름
KTCTheme.colorScheme.text.primary
KTCTheme.colorScheme.text.secondary
KTCTheme.colorScheme.surface
KTCTheme.colorScheme.icon.default
KTCTheme.colorScheme.button.text
KTCTheme.colorScheme.inputField.container
KTCTheme.colorScheme.line.divider1px

// ❌ 절대 금지
MaterialTheme.colorScheme.onSurface     // MaterialTheme 금지
HmmColor.Blue1                          // HmmColor 직접 사용 금지
Color(0xFF000000)                       // 하드코딩 금지
```

**새 색상이 필요한 경우만 임시 하드코딩 허용**:
```kotlin
// TODO: 새로운 색상 필요 - KTCColorScheme에 추가 요청
// 용도: [사용 목적]
// 제안 색상: Color(0xFFXXXXXX)
Box(modifier = Modifier.background(Color(0xFFXXXXXX)))
```

### KTCColorScheme 주요 속성

| 카테고리 | 속성 | 용도 |
|---------|------|------|
| `text` | `.primary` | 주요 텍스트 |
| `text` | `.secondary` | 보조 텍스트 |
| `text` | `.third` | 3차 텍스트 |
| `text` | `.disable` | 비활성화 텍스트 |
| `text` | `.white` | 흰색 텍스트 |
| `inputField` | `.text`, `.hint`, `.border`, `.container` | 입력 필드 |
| `tab` | `.text`, `.textSelected`, `.container`, `.containerSelected` | 탭 |
| `icon` | `.default`, `.inverse` | 아이콘 |
| `button` | `.text`, `.textDisable`, `.containerDisable` | 버튼 |
| `line` | `.topbar`, `.divider1px`, `.divider10px`, `.border` | 구분선 |
| 기본 | `.surface` | 배경 |
| 기본 | `.container` | 컨테이너 |
| 기본 | `.error` | 에러 |

## 타이포그래피 규칙

**`KTCTheme.typography`만 사용**합니다:

```kotlin
// ✅ 올바름
KTCTheme.typography.bold20
KTCTheme.typography.medium16
KTCTheme.typography.regular14

// ❌ 금지
fontSize = 20.sp, fontWeight = FontWeight.Bold
```

주요 스타일: `bold32`~`bold10`, `medium24`~`medium8`, `regular24`~`regular10`, `black22`, `light22`

## 주요 컴포넌트

UI 구현 시 커스텀 컴포넌트 대신 디자인 시스템 컴포넌트를 우선 사용합니다.

| 용도 | 컴포넌트 | 비고 |
|------|---------|------|
| 바텀 고정 버튼 | `LargeButton` | 전체 너비, 사각형 |
| 일반 버튼 | `RegularButton` | `ButtonLocation.Bottom/NonBottom/Modal` |
| 테두리 버튼 | `BorderButton` | |
| 선택 버튼 | `SelectorButton` | |
| 텍스트 입력 | `HmInputField` | BasicTextField 기반 |
| 체크박스 | `HmCheckBox` | |
| 라디오 | `HmRadio` | |
| 스위치 | `HmSwitch` | |
| 상단바 | `HmTopAppbar` | `HmTopAppbarType.Default/Close/Delete` |
| 기본 스캐폴드 | `HmDefaultScaffold` | statusBar 패딩 자동 |
| 일반 스캐폴드 | `HmNormalScaffold` | TopAppbar 포함 |
| 새로고침 스캐폴드 | `HmRefreshScaffold` | Pull to Refresh |
| 다이얼로그 (커스텀) | `BaseDialog` | 버튼 직접 구성 |
| 다이얼로그 (기본) | `DefaultDialog` | 텍스트 + 확인/취소 |
| 다이얼로그 (확인만) | `ConfirmDialog` | 단일 버튼 |
| 바텀시트 (커스텀) | `BaseBottomSheet` | |
| 바텀시트 (기본) | `DefaultBottomSheet` | 확인 버튼 포함 |
| 모달 버튼 | `ModalButton` | 다이얼로그/바텀시트 내부용 |
| 가로 구분선 | `HmHorizontalDivider` | |
| 세로 간격 | `HmVerticalSpacer` | |
| 가로 간격 | `HmHorizontalSpacer` | |
| 필터 목록 | `HmFilter` + `FilterBuilder` | LazyRow |
| 로딩 | `HmLoading` | |
| 라벨 | `HmLabel`, `HmBorderLabel` | |
| 드롭다운 | `HmDropDown` | |
| 애니메이션 | `HmFadeAnimatedVisibility` | 상태 기반 페이드 |
| 전체 화면 다이얼로그 | `FullScreenDialog` | |

**상세 API는** `.github/prompts/design-system.prompt.md` 또는 `core/designsystem/src/main/kotlin/ktc/cargo/driver/designsystem/component/` 디렉토리를 직접 참조하세요.

## UI 작성 규칙

1. **Preview**: `@ThemePreviews` + `HmmTheme { }` 감싸기
2. **클릭 영역**: `RoundedCornerShape(4.dp)` Clip 처리
3. **중복 클릭 방지**: `clickableSingle { }` 사용
4. **폰트 크기 고정**: `KTCTheme.typography.bold20.nonScaledSp`
5. **다크 모드**: `KTCTheme.colorScheme` 사용 시 자동 대응
