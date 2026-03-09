---
description: "로컬 저장소 Data Layer 구현 - Room Entity, DAO 또는 DataStore 기반 LocalDataSource"
model: claude-haiku-4-5-20251001
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Local Data Layer Skill

$ARGUMENTS 를 기반으로 Local Data Layer를 구현합니다.

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **저장소 타입**: `room` 또는 `datastore`
- **기능명**: PascalCase (예: `Hand`, `Player`)
- **데이터 구조**: 저장할 필드

## 전제 조건

Domain Layer가 먼저 구현되어 있어야 합니다:
- Domain Model (`:domain:model/`)
- Repository Interface (`:domain:repository/`)

---

# Room Database 구현

`$ARGUMENTS`에 `room`이 포함된 경우 이 섹션을 따릅니다.

## 현재 DB 구조

프로젝트는 Kotlin Multiplatform + Room을 사용합니다.
패키지: `com.hand.log.database`

### 테이블 구조 (4개 테이블)

#### hands (핸드 기본 정보)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | String (PK) | 핸드 ID |
| date | Long | 플레이 날짜 (timestamp) |
| tableSize | Int | 테이블 인원 (2-10) |
| heroPosition | String? | 히어로 포지션 |
| potSize | Double? | 팟 사이즈 |
| notes | String? | 메모 |
| createdAt | Long | 생성일 (timestamp) |

#### hand_players (각 핸드의 플레이어)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | String (PK) | |
| handId | String (FK → hands) | 핸드 |
| position | String | 포지션 (BTN, SB 등) |
| card1Rank | String? | 카드1 랭크 |
| card1Suit | String? | 카드1 수트 |
| card2Rank | String? | 카드2 랭크 |
| card2Suit | String? | 카드2 수트 |
| result | String? | win/lose/split |

#### hand_actions (액션 기록)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | String (PK) | |
| handId | String (FK → hands) | 핸드 |
| street | String | preflop/flop/turn/river |
| position | String | 포지션 |
| actionType | String | fold/check/call/bet/raise/all-in |
| amount | Double? | 금액 |
| actionOrder | Int | 액션 순서 |

#### hand_community_cards (커뮤니티 카드)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | String (PK) | |
| handId | String (FK → hands) | 핸드 |
| cardIndex | Int | 0-4 (flop 0-2, turn 3, river 4) |
| rank | String | 랭크 |
| suit | String | 수트 |

### 특징
- 모든 FK에 `onDelete = CASCADE` 적용 (hand 삭제 시 관련 데이터 자동 삭제)
- TypeConverter 없이 String 타입 사용
- KMP (commonMain) 기반

## Room 구현 순서 (5단계)

### 1단계: Entity 정의

**위치**: `local/database/src/commonMain/kotlin/com/hand/log/database/entity/`

**규칙**:
- `@Entity(tableName = "...")` 사용
- `@PrimaryKey` 지정
- FK가 필요한 경우 `@ForeignKey`로 CASCADE 설정
- 패키지: `com.hand.log.database.entity`

**참고 패턴**:
```kotlin
package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
	tableName = "hand_players",
	foreignKeys = [
		ForeignKey(
			entity = HandEntity::class,
			parentColumns = ["id"],
			childColumns = ["handId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class HandPlayerEntity(
	@PrimaryKey val id: String,
	val handId: String,
	val position: String,
	val card1Rank: String? = null,
	val card1Suit: String? = null,
	val card2Rank: String? = null,
	val card2Suit: String? = null,
	val result: String? = null
)
```

**참고 파일**:
- `local/database/src/commonMain/kotlin/com/hand/log/database/entity/HandEntity.kt`
- `local/database/src/commonMain/kotlin/com/hand/log/database/entity/HandPlayerEntity.kt`
- `local/database/src/commonMain/kotlin/com/hand/log/database/entity/HandActionEntity.kt`
- `local/database/src/commonMain/kotlin/com/hand/log/database/entity/HandCommunityCardEntity.kt`

### 2단계: DAO 정의

**위치**: `local/database/src/commonMain/kotlin/com/hand/log/database/dao/`

**규칙**:
- `@Dao interface`
- `@Insert`, `@Update`, `@Delete`, `@Query`, `@Transaction` 사용
- `Flow<T>` (실시간 구독) 또는 `suspend fun` (일회성) 선택
- 패키지: `com.hand.log.database.dao`

**참고 패턴**:
```kotlin
package com.hand.log.database.dao

import androidx.room.*
import com.hand.log.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HandHistoryDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertHand(hand: HandEntity)

	@Query("SELECT * FROM hands ORDER BY date DESC")
	fun getAllHands(): Flow<List<HandEntity>>

	@Transaction
	suspend fun insertCompleteHand(
		hand: HandEntity,
		players: List<HandPlayerEntity>,
		actions: List<HandActionEntity>,
		communityCards: List<HandCommunityCardEntity>
	) {
		insertHand(hand)
		insertPlayers(players)
		insertActions(actions)
		insertCommunityCards(communityCards)
	}
}
```

**참고 파일**:
- `local/database/src/commonMain/kotlin/com/hand/log/database/dao/HandHistoryDao.kt`

### 3단계: CompleteHandHistory 모델 및 변환 함수

**위치**: `local/database/src/commonMain/kotlin/com/hand/log/database/model/`

Entity ↔ 도메인 모델 변환을 위한 데이터 클래스와 확장 함수입니다.

**참고 파일**:
- `local/database/src/commonMain/kotlin/com/hand/log/database/model/CompleteHandHistory.kt`

### 4단계: AppDatabase에 등록

**위치**: `local/database/src/commonMain/kotlin/com/hand/log/database/AppDatabase.kt`

1. `@Database(entities = [...])` 배열에 새 Entity 추가
2. `abstract fun {name}Dao(): {Name}Dao` 추가

**참고 파일**:
- `local/database/src/commonMain/kotlin/com/hand/log/database/AppDatabase.kt`

### 5단계: Factory (플랫폼별)

**위치**:
- `local/database/src/commonMain/kotlin/com/hand/log/database/di/Factory.kt`
- `local/database/src/androidMain/kotlin/com/hand/log/database/di/Factory.android.kt`
- `local/database/src/iosMain/kotlin/com/hand/log/database/di/Factory.native.kt`

## Room 파일 위치 요약

| 단계 | 파일 위치 |
|------|-----------|
| 1. Entity | `local/database/src/commonMain/kotlin/com/hand/log/database/entity/` |
| 2. DAO | `local/database/src/commonMain/kotlin/com/hand/log/database/dao/` |
| 3. Model | `local/database/src/commonMain/kotlin/com/hand/log/database/model/` |
| 4. DB 등록 | `local/database/src/commonMain/kotlin/com/hand/log/database/AppDatabase.kt` |
| 5. Factory | `local/database/src/commonMain/kotlin/com/hand/log/database/di/Factory.kt` |

---

# DataStore 구현

`$ARGUMENTS`에 `datastore`가 포함된 경우 이 섹션을 따릅니다.

## DataStore 구현 순서

**위치**: `local/datastore/src/commonMain/kotlin/com/hand/log/local/datastore/`

**참고 파일**:
- `local/datastore/src/commonMain/kotlin/com/hand/log/local/datastore/CartDataStore.kt`
- `local/datastore/src/commonMain/kotlin/com/hand/log/local/datastore/di/AppContainer.kt`
- `local/datastore/src/commonMain/kotlin/com/hand/log/local/datastore/di/Factory.kt`

---

## 체크리스트

### Room
- [ ] Entity에 `@Entity`, `@PrimaryKey` 있는지
- [ ] FK 관계에 `onDelete = CASCADE` 설정했는지
- [ ] DAO가 `@Dao interface`인지
- [ ] `@Transaction`으로 복합 작업을 묶었는지
- [ ] AppDatabase에 Entity, DAO 등록했는지

### DataStore
- [ ] Prefs 모델이 `@Serializable data class`인지
- [ ] 모든 필드에 기본값이 있는지