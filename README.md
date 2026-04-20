# HandyLog

포커 핸드 히스토리를 기록하고 분석하는 **Kotlin Multiplatform** 앱입니다.
Compose Multiplatform 기반으로 Android와 iOS를 단일 코드베이스로 지원합니다.

## Features

- **핸드 레코딩** — 프리플랍부터 리버까지 액션을 단계별로 기록하는 멀티스텝 폼
- **핸드 분석** — 핸드 랭킹 평가, 사이드팟 분배, 포지션별 통계
- **테이블 관리** — 토너먼트/캐시게임 세션 생성, 블라인드 구조 설정, 좌석 배치
- **플레이어 DB** — 상대 플레이어 저장, 성향 태깅, 플레이어별 핸드 히스토리 조회
- **다국어 지원** — 한국어, English, 日本語, 中文
- **다크/라이트 테마** — 시스템 설정 연동 및 수동 전환

## Tech Stack

| 영역 | 기술 |
|---|---|
| **Language** | Kotlin 2.2 (Multiplatform) |
| **UI** | Compose Multiplatform 1.10, Material3 |
| **Navigation** | Navigation3 (Type-safe @Serializable Route) |
| **DI** | Koin 4.1 |
| **Local DB** | Room 2.8 + SQLite Bundled |
| **Preferences** | DataStore |
| **Networking** | Ktor 3.1 (OkHttp / Darwin) |
| **Serialization** | KotlinX Serialization 1.8 |
| **Async** | Coroutines + Flow |
| **Code Quality** | KtLint, Detekt |

## Architecture

Clean Architecture + MVVM 패턴을 멀티모듈로 구현했습니다.

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│  Feature Modules (Screen, ViewModel, Route) │
├─────────────────────────────────────────────┤
│               Domain Layer                  │
│       Model  ·  Repository  ·  UseCase      │
├─────────────────────────────────────────────┤
│                Data Layer                   │
│    RepositoryImpl  ·  DataSource  ·  Local  │
└─────────────────────────────────────────────┘
```

## Module Structure

```
composeApp/                  # 앱 진입점 (Android + iOS)
core/
├── designsystem/            # HandyTheme, 공통 컴포넌트
├── navigation/              # Route 정의, MainNavigator
├── res/                     # 드로어블, 문자열 리소스 (ko/en/ja/zh)
├── ui/                      # 공유 Composable 유틸
├── utils/                   # Toast, DateTime, StatusBar
└── common/                  # 공통 Kotlin 유틸
domain/
├── model/                   # HandRecord, PokerTable, Card, Position 등
├── repository/              # Repository 인터페이스
└── usecase/                 # 비즈니스 로직
data/
├── datasource/              # DataSource 인터페이스
└── repositoryImpl/          # Repository 구현체
local/
├── database/                # Room Entity, DAO
└── datastore/               # DataStore Preferences
feature/
├── main/                    # 바텀 네비게이션
├── home/                    # 대시보드
├── table/                   # 테이블 관리 (home, table-edit, player-setup)
├── record/                  # 핸드 기록 위자드
├── hand-detail/             # 핸드 상세 분석
├── players/                 # 플레이어 관리 (home, hands, players-edit)
└── settings/                # 설정 (home, betsize)
build-logic/                 # Convention Plugin
```

## Requirements

- Android: API 26+ (Android 8.0)
- iOS: Kotlin/Native
- JDK 17+
