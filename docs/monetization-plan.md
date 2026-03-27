# HandyLog 무료/유료 버전 구분 전략

## 1. 기본 방향

**Freemium 모델** — 핵심 기능은 무료로 제공하되, 파워 유저가 필요로 하는 고급 기능에 제한을 두는 방식.
무료 버전만으로도 충분히 유용해야 하고, 유료 전환은 "불편함 해소"가 아닌 "더 많은 가치 제공"이어야 한다.

---

## 2. 무료 버전 (Free)

### 포함되는 기능
- 테이블 생성 및 관리 (최대 **3개** 테이블)
- 핸드 기록 (테이블당 **최대 20핸드**)
- 플레이어 셋업 (이름, 성향, 메모)
- 핸드 상세 보기 (테이블 뷰, 액션 그리드, 결과)
- BB 단위 토글
- 다크/라이트 테마

### 제한사항
| 항목 | 무료 | 유료 |
|------|------|------|
| 테이블 수 | 3개 | 무제한 |
| 테이블당 핸드 수 | 20개 | 무제한 |
| 마킹 플레이어 저장 | 5명 | 무제한 |
| 핸드 히스토리 텍스트 공유 | O | O |
| 핸드 이미지 공유/다운로드 | X (워터마크) | O |
| 핸드 데이터 CSV 내보내기 | X | O |
| 통계/분석 | X | O |

---

## 3. 유료 버전 (Pro)

### 결제 모델 옵션

#### A. 일회성 구매 (Lifetime)
- 장점: 사용자 저항 낮음, 구현 단순
- 단점: 지속적 수익 없음
- 가격대: $9.99 ~ $14.99

#### B. 구독 (Subscription)
- 장점: 지속적 수익, 기능 추가 동기 부여
- 단점: 유틸리티 앱에 구독 거부감
- 가격대: $2.99/월 또는 $19.99/년

#### C. 혼합 (권장)
- **월간 구독**: $2.99/월
- **연간 구독**: $19.99/년 (약 30% 할인)
- **평생 이용권**: $29.99 (일회성)

### Pro 전용 기능
- 무제한 테이블 & 핸드
- 무제한 마킹 플레이어
- 이미지 공유 (워터마크 없음)
- CSV/JSON 데이터 내보내기
- 세션 통계 (수익률, 포지션별 승률, 시간대별 분석)
- 상대 플레이어 통계 (성향별 승률)
- 커스텀 테마/컬러

---

## 4. 기술 구현 방안

### 4.1 구분 방식

```
domain/model/SubscriptionStatus.kt
```

```kotlin
enum class SubscriptionTier {
    FREE,
    PRO,
}

data class SubscriptionStatus(
    val tier: SubscriptionTier,
    val expiresAt: Long? = null, // null = lifetime
)
```

### 4.2 제한 체크 레이어

```kotlin
// domain/usecase/CheckFeatureLimitUseCase.kt
class CheckFeatureLimitUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val tableRepository: PokerTableRepository,
) {
    suspend fun canCreateTable(): Boolean
    suspend fun canRecordHand(tableId: String): Boolean
    suspend fun canExportImage(): Boolean
    suspend fun canSaveMarking(): Boolean
}
```

### 4.3 저장 위치

- **구독 상태**: DataStore (로컬) + 서버 영수증 검증
- **구매 처리**:
  - Android: Google Play Billing Library
  - iOS: StoreKit 2
- **KMP 공통**: `expect/actual`로 플랫폼별 결제 구현

### 4.4 모듈 구조

```
core/billing/          ← expect/actual 결제 인터페이스
  src/commonMain/      ← SubscriptionRepository, BillingService interface
  src/androidMain/     ← Google Play Billing 구현
  src/iosMain/         ← StoreKit 2 구현

domain/subscription/   ← UseCase (CheckFeatureLimit, RestorePurchase 등)

feature/subscription/  ← 구독 화면 UI, Paywall 다이얼로그
```

### 4.5 Paywall 노출 시점

- 테이블 3개 초과 생성 시
- 핸드 20개 초과 기록 시
- 이미지 공유/다운로드 탭 시
- 데이터 내보내기 탭 시
- 통계 탭 진입 시

Paywall은 **ModalBottomSheet** 형태로, 기능 설명 + 가격 + 구매 버튼을 포함.

---

## 5. 단계별 적용 로드맵

### Phase 1 — 출시 (무료 전용)
- 모든 기능 무료 제공
- 사용자 확보 및 피드백 수집
- 앱 안정성 확보

### Phase 2 — 제한 도입
- 테이블/핸드 수 제한 추가
- Pro 뱃지 UI 준비
- Paywall 다이얼로그 구현

### Phase 3 — 결제 연동
- Google Play Billing / StoreKit 2 연동
- 구독 화면 구현
- 영수증 검증 (로컬 우선, 서버는 선택)

### Phase 4 — Pro 전용 기능
- 통계/분석 기능 개발
- CSV 내보내기
- 커스텀 테마

---

## 6. 주의사항

- **무료 사용자 경험 훼손 금지**: 핵심 기록 기능은 제한 내에서 완전히 동작해야 함
- **기존 데이터 보호**: 유료→무료 전환 시 기존 데이터 삭제하지 않고, 읽기만 허용
- **오프라인 동작**: 결제 상태는 로컬에 캐시하여 오프라인에서도 Pro 기능 사용 가능
- **복원 기능 필수**: "구매 복원" 버튼 제공 (특히 iOS 심사 필수 요건)
