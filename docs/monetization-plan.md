# HandyLog 유료화 전략

## 1. 수익 모델

### 추천: Freemium + 일회성 구매 (Lifetime)

| 모델 | 장점 | 단점 |
|------|------|------|
| **구독 (월/연)** | 안정적 반복 수익 | 포커 앱 특성상 사용 빈도 불규칙, 이탈률 높음 |
| **일회성 구매 (Lifetime)** | 전환율 높음, 포커 유저 선호 | 반복 수익 없음 |
| **혼합 (권장)** | Lifetime으로 시작, 추후 구독 추가 가능 | 두 모델 동시 관리 필요 |

**1차 권장**: Lifetime 단일 상품 ($9.99~$14.99)
- 포커 플레이어는 세션 기반 사용 → 구독보다 일회성 구매 전환율이 높음
- 클라우드 동기화 등 서버 기능 추가 시 구독 모델 도입 검토

---

## 2. 무료 vs 프로 기능 분류

### 설계 원칙: "기록은 자유롭게, 분석과 공유는 프로에서"

기록 자체를 제한하면 유저 이탈이 심함. 테이블 수와 분석/공유 기능으로 간접 제한.

### 무료 (Free)

| 기능 | 상세 |
|------|------|
| 테이블 생성 | **최대 3개** 활성 테이블 |
| 핸드 기록 | 테이블당 **최대 30핸드** |
| 보드/핸드 입력 | 전체 기능 |
| 쇼다운 결과 | 족보 판정, 승패 표시 |
| 핸드 상세 | 기본 뷰 |
| 텍스트 공유 | 핸드 히스토리 텍스트 복사 |
| 테마 | 라이트/다크/자동 |
| 베팅 프리셋 | 기본값 고정 (2, 2.5, 3, 4, 5BB / 33, 50, 75, 100%) |
| 마킹 플레이어 | **최대 5명** |

### 프로 (Pro)

| 기능 | 상세 | 구현 난이도 |
|------|------|------------|
| **무제한 테이블** | 테이블 개수 제한 해제 | 낮음 |
| **무제한 핸드** | 테이블당 핸드 수 제한 해제 | 낮음 |
| **무제한 마킹 플레이어** | 저장 플레이어 제한 해제 | 낮음 |
| **프리셋 커스터마이징** | 베팅/팟 프리셋 자유 편집 | 낮음 |
| **이미지 공유** | 핸드 PNG 이미지 저장/공유 (워터마크 없음) | 낮음 |
| **CSV/JSON 내보내기** | 핸드 히스토리 데이터 내보내기 | 중간 |
| **플레이어 통계** | 플레이어별 승률, 수익, 핸드 수 | 중간 |
| **세션 리포트** | 테이블별 수익 그래프, BB/hr | 중간 |
| **핸드 필터/검색** | 포지션/결과/날짜/상대별 핸드 검색 | 중간 |

---

## 3. Paywall 트리거

| 시점 | 메시지 |
|------|--------|
| 4번째 테이블 생성 시 | "프로로 업그레이드하면 무제한 테이블" |
| 31번째 핸드 기록 시 | "이 테이블에서 더 많은 핸드를 기록하세요" |
| 6번째 플레이어 마킹 시 | "프로에서 무제한 플레이어 마킹" |
| 이미지 공유 버튼 탭 | "프로 기능입니다" + 이미지 미리보기 표시 |
| 프리셋 수정 시도 | "프로에서 자유롭게 커스터마이징" |
| 세션 종료 시 | "이번 세션 +12.5BB! 자세한 통계는 프로에서" |

Paywall은 **ModalBottomSheet** 형태 — 기능 설명 + 가격 + 구매 버튼.

---

## 4. 기술 구현

### 4-1. 모듈 구조

```
core/billing/                  ← 결제 인터페이스 (expect/actual)
  src/commonMain/              ← BillingService, SubscriptionRepository
  src/androidMain/             ← Google Play Billing Library
  src/iosMain/                 ← StoreKit 2

domain/repository/
  └── ProRepository.kt        ← isPro: Flow<Boolean>, purchase(), restore()

domain/usecase/
  └── CheckFeatureLimitUseCase.kt

local/datastore/
  └── ProDataStore.kt          ← 구매 상태 로컬 캐시

feature/subscription/          ← Paywall UI, 구매 화면
```

### 4-2. Pro 상태 관리

```kotlin
// domain/model/ProStatus.kt
enum class ProTier {
    FREE,
    PRO,
}

data class ProStatus(
    val tier: ProTier,
    val purchasedAt: Long? = null,
)
```

### 4-3. Feature Gating

```kotlin
// domain/usecase/CheckFeatureLimitUseCase.kt
class CheckFeatureLimitUseCase(
    private val proRepository: ProRepository,
    private val tableRepository: PokerTableRepository,
    private val playerRepository: SavedPlayerRepository,
) {
    suspend fun canCreateTable(): Boolean          // Free: < 3
    suspend fun canRecordHand(tableId: String): Boolean  // Free: < 30/table
    suspend fun canSavePlayer(): Boolean           // Free: < 5
    suspend fun canShareImage(): Boolean           // Pro only
    suspend fun canCustomizePresets(): Boolean      // Pro only
    suspend fun canExportData(): Boolean           // Pro only
}
```

### 4-4. Paywall UI

```kotlin
// core/designsystem/component/
ProBadge.kt          // "PRO" 라벨 배지 (기능 옆에 표시)
ProPaywall.kt        // 업그레이드 BottomSheet
```

### 4-5. 플랫폼별 결제

| 플랫폼 | 라이브러리 | 비고 |
|--------|-----------|------|
| Android | `com.android.billingclient:billing-ktx` | Non-consumable IAP |
| iOS | StoreKit 2 | Swift interop, `@objc` export |
| KMP 공용 | `expect/actual` 패턴 | interface는 commonMain |

---

## 5. 로드맵

### Phase 1 — 출시 (현재)
- 모든 기능 무료 제공
- 유저 확보 및 피드백 수집

### Phase 2 — 제한 도입 + Paywall (1~2주)
- [ ] `ProRepository` + `ProDataStore` 구현
- [ ] `CheckFeatureLimitUseCase` 구현
- [ ] 테이블/핸드/플레이어 수 제한 적용
- [ ] 프리셋 수정 제한
- [ ] `ProBadge`, `ProPaywall` UI 구현
- [ ] 설정 화면에 "프로 업그레이드" 섹션 추가

### Phase 3 — 결제 연동 (1~2주)
- [ ] `core/billing` 모듈 생성
- [ ] Android BillingClient 연동
- [ ] iOS StoreKit 2 연동
- [ ] 구매 복원 기능 (iOS 심사 필수)

### Phase 4 — 프로 전용 기능 (2~4주)
- [ ] 이미지 공유 gating + 무료 시 워터마크
- [ ] 플레이어 통계 화면
- [ ] 세션 리포트 (수익 그래프)
- [ ] 핸드 필터/검색
- [ ] CSV/JSON 내보내기

---

## 6. 주의사항

- **무료 사용자 경험 훼손 금지**: 핵심 기록 기능은 제한 내에서 완전히 동작
- **기존 데이터 보호**: Pro → Free 전환 시 기존 데이터 삭제하지 않고 읽기 허용
- **오프라인 동작**: 구매 상태 로컬 캐시로 오프라인에서도 Pro 기능 사용 가능
- **구매 복원 필수**: iOS 심사 필수 요건 — 설정에 "구매 복원" 버튼
- **가격**: 출시 프로모션 $6.99 → 정상가 $9.99~$14.99
