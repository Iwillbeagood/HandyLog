# Action 로그가 핸드의 입력 단일 진실의 원천이다 (파생값은 저장 시 materialize)

핸드의 **입력** 단일 진실은 스트릿별 `Action` 로그다. 스택·팟·사이드 팟·승자 분배 같은 **파생값**은 그 순수 함수이며(`HandRecord.getPotAtStreet`, `seatInvestments`, `getFinalStacks`, `winnerSeats`), Action에서 언제든 재계산할 수 있다.

## 결정 (2026-07, refined)

파생값을 **매 읽기마다 재계산하지 않고, 저장 시점에 한 번 계산해 `HandRecord.results`(`HandResults`)로 DB에 materialize**한다. 계산은 단일 쓰기 퍼널인 `HandRecordRepositoryImpl.saveHand`에서 수행하고(`HandEvaluator`는 데이터 레이어가 주입), 저장 직전 스택 보존 불변식(`Σ초기 ≈ Σ최종`)을 자가검증해 어긋나면 로깅한다(저장은 막지 않음).

- **원천 관계는 유지**: Action = 입력 원천, `results` = 그로부터의 재계산 가능한 캐시. 둘이 어긋나면 Action이 이긴다(수동 보정 없음).
- **읽기 경로**: `hand.results`를 읽되, 미저장 핸드를 위해 순수 함수로 폴백(`HandRecord.potAt` 등). 순수 계산 함수는 삭제하지 않고 유일한 계산 구현으로 유지한다.

## 근거

- **성능**: 목록·상세가 O(1) 필드 읽기. `getFinalStacks`가 `evaluator`를 부르는 비용을 읽기마다 치르지 않고, 향후 포지션별 수익 등 히스토리 집계도 컬럼 쿼리로 가능.
- **정확성**: 계산을 한 곳으로 모아 불변식으로 자가검증 → DB에 정합한 데이터만 저장. 로직 변경 시에도 과거 표시 결과가 소급 변동하지 않음.
- **단순화**: 읽기부(`ActionGridSection`, `HandHistoryFormatter`)에서 계산·`evaluator` 배선 제거.

## 대가 / 주의

- 저장값은 계산 로직을 **동결**하므로, 계산 버그는 재저장 전까지 남는다 → 순수 함수는 `HandRecordPotTest`/`HandEvaluatorTest`로 잠근 뒤 materialize.
- 앱 배포 전이라 DB 스키마는 마이그레이션 없이 직접 변경(`version` 상향, `fallbackToDestructiveMigration` 사용).
