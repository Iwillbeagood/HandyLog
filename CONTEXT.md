# HandyLog — Domain Context

HandyLog은 포커 핸드 트래킹 앱이다. 플레이어(=Hero)가 라이브 테이블에서 직접 플레이한 핸드를 기록하고 나중에 복기한다. 이 문서는 그 도메인의 **용어집(ubiquitous language)**이다 — 표제어가 정식 용어이고, `_Avoid_`에 적힌 단어는 코드·문서에서 표류하지 말아야 할 동의어다.

구현 세부사항은 담지 않는다. 용어의 "무엇인가"만 정의한다.

## Table & Hand

**PokerTable**:
한 번의 테이블 착석 — 날짜·장소·게임 종류와 착석한 플레이어들. 모든 핸드는 하나의 테이블에 속해 기록된다.
_Avoid_: Session, Game, Room

**Hand**:
프리플랍부터 결과까지 한 판 전체. `HandRecord`로 기록되며, 사용자가 로그하고 복기하는 최소 단위.
_Avoid_: Round, Deal, Game
※ "hand"는 세 가지로 쓰인다 — 한 판(이 항목), 들고 있는 두 장([[Hole cards]]), 만들어진 족보([[Hand ranking]]).

**Hero**:
사용자 본인. 모든 핸드는 Hero의 시점에서 기록되며, 한 핸드에 Hero 좌석은 정확히 하나다.
_Avoid_: Me, User, Self

**Seat**:
테이블의 번호가 매겨진 자리(1-base). 누가 무엇을 했는지의 안정적 식별자이며, 포지션과 플레이어는 좌석에서 파생된다.
_Avoid_: Chair, Slot, Index

## Players

**Player**:
한 테이블에 착석해 있는 동안의 좌석 점유자.
_Avoid_: Participant

**HandPlayer**:
특정 한 핸드에서의 플레이어 관여 정보 — 홀 카드, 시작 스택, 쇼다운 결과.
_Avoid_: Entrant

**SavedPlayer**:
테이블을 넘어 지속되는 상대 프로필. 성향과 메모를 지닌다.
_Avoid_: Contact, Profile, Opponent record

**Marked player**:
어떤 핸드의 좌석이 [[SavedPlayer]]에 연결된 상태. 그 상대의 리드와 히스토리가 핸드를 넘어 따라붙는다.
_Avoid_: Tagged, Linked, Named

**Tendency**:
플레이어의 플레이 성향 요약 — TAG(tight-aggressive), LAG, Shark, Fish 등.
_Avoid_: Style, Type, Archetype

## Cards

**Card**:
한 장의 카드 — 랭크(2–A)와 슈트(♠♥♦♣).

**Hole cards**:
한 플레이어에게 딜된 두 장의 비공개 카드. 표시 용어는 Hole cards로 통일하되, 도메인 타입명은 `PocketCards`를 유지한다(`core/ui`의 `HoleCards` Composable과 이름 충돌을 피하기 위한 의도된 예외).
_Avoid_: Pocket cards, Down cards

**Board**:
모두가 공유하는 커뮤니티 카드 — 플랍·턴·리버에 걸쳐 최대 5장.
_Avoid_: Community cards(타입명으로), Table cards

**Kicker**:
같은 족보끼리의 우열을 가르는 카드.
_Avoid_: Tiebreaker

## Streets & Actions

**Street**:
베팅 라운드 — Preflop, Flop, Turn, River.
_Avoid_: Round, Phase, Stage

**Preflop / Flop / Turn / River**:
순서대로의 네 스트릿. Flop은 보드 3장, Turn·River는 각 1장을 공개하고, Preflop은 보드가 없다.

**Action**:
한 스트릿에서의 플레이어의 한 수 — 타입·금액·전후 스택.
_Avoid_: Move, Play, Event

**Action type**:
Fold, Check, Call, Bet, Raise, All-in.
_Avoid_: Decision

**Bet level**:
한 스트릿 안에서 베팅/레이즈의 서수(오픈, 3-bet, 4-bet…).
_Avoid_: Raise count

## Position & Blinds

**Position**:
버튼을 기준으로 한 좌석의 전략적 역할 — BTN, SB, BB, UTG, HJ, CO 등. 좌석·버튼·인원 수에서 파생되며 저장하지 않는다.
_Avoid_: Spot, Role

**Button**:
딜러 버튼을 가진 좌석. 다른 모든 포지션을 확정하는 기준점.
_Avoid_: Dealer

**Blinds**:
강제 베팅 — 스몰 블라인드(SB)와 빅 블라인드(BB), 선택적 스트래들.
_Avoid_: Forced bets

**Straddle**:
딜 전에 BB보다 크게 올려 거는 선택적 강제 베팅.

**Big blind ante**:
빅 블라인드 좌석이 테이블 앤티까지 대신 내는 토너먼트 옵션.
_Avoid_: Ante(BB-앤티 변형을 특정할 때 단독 사용)

**Game type**:
테이블이 토너먼트(Tournament)인지 캐시 게임(Cash)인지.
_Avoid_: Format, Mode

## Money

**Stack**:
플레이어가 가진 칩. `initialStack`은 핸드 시작 시점, `stackBefore`/`stackAfter`는 한 액션 전후.
_Avoid_: Chips, Bankroll

**Investment**:
한 좌석이 그 핸드에서 블라인드 포함 모든 스트릿에 걸쳐 투입한 총액.
_Avoid_: Contribution, Wagered

**Pot**:
핸드에서 다투는 칩. 좌석별 투입액이 다르면 투입 레벨 기준으로 사이드 팟(side pot)으로 나뉜다.
_Avoid_: Prize

**Result**:
Hero의 그 핸드 순수 칩 변화(양수 = 이득). [[Outcome]]과 구분된다.
_Avoid_: Profit, PnL(정식 용어로)

## Showdown & Result

**Showdown**:
핸드 끝에서 남은 플레이어들이 핸드를 공개해 비교하는 것. 홀 카드가 알려진 플레이어만 등장한다.
_Avoid_: Reveal, Face-off

**Hand ranking**:
플레이어가 만든 족보 — Royal Flush부터 High Card까지, 그리고 쇼다운 없이 끝났을 때의 "Win by fold".
_Avoid_: Rank(그건 카드의 값), Hand strength, Score

**Outcome**:
좌석별 쇼다운 판정 — Win, Lose, Split. [[Result]](Hero 칩)과 [[Hero result type]]과 구분된다.
_Avoid_: Verdict, Status

**Split**:
둘 이상이 동점이라 팟을 나눠 갖는 아웃컴.
_Avoid_: Chop, Tie

**Fold win**:
나머지가 모두 폴드해 좌석 하나만 남아 쇼다운 없이 이긴 핸드.
_Avoid_: Walk, Uncontested win

**Hero result type**:
Hero 핸드가 끝난 방식을 fold/showdown × win/lose/split로 요약한 다섯 갈래.
_Avoid_: Result category

---

## 용어 충돌 — 해소 결과

domain-modeling 과정에서 코드와 대조하며 발견한 세 지점의 결정:

1. **Hole cards vs Pocket cards** — 표시 용어는 **Hole cards**로 확정. 도메인 타입명 `PocketCards`는 `core/ui`의 `HoleCards` Composable과의 이름 충돌을 피하려는 의도된 예외로 **유지**한다.
2. **"Hand"의 삼중 의미** — `HandRecord`(한 판) / 홀 카드 / `HandRanking`(족보). 홀 카드를 가리키던 도메인 프로퍼티 `HandRecord.heroHand`는 **`heroHoleCards`로 리네임 완료**(State/Effect 계층의 `heroHand` 필드는 별개 개념이라 유지).
3. **Result vs Outcome vs HeroResultType** — 세 개념은 실제로 별개다: `result`=Hero 순수 칩(Double), `Outcome`=쇼다운 판정 enum(WIN/LOSE/SPLIT), `HeroResultType`=fold/showdown×win/lose/split 5분류. 표준 포커 용어에 맞으므로 리네임하지 않고 **경계만 확정**한다.
