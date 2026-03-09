---
description: "서버 API 연동 Data Layer 구현 - DTO, Retrofit API, Mapper, RemoteDataSource, RepositoryImpl, Hilt 모듈 등록"
model: claude-sonnet-4-5-20250929
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
---

# Remote Data Layer Skill

$ARGUMENTS 를 기반으로 Remote API Data Layer를 구현합니다.

## 전제 조건

Domain Layer가 먼저 구현되어 있어야 합니다:
- Domain Model (`:domain:model/`)
- Repository Interface (`:domain:repository/`)

## 필요 인자

`$ARGUMENTS`에서 다음 정보를 파악합니다:
- **기능명**: PascalCase (예: `Block`, `Cargo`, `Account`)
- **API 스펙**: 엔드포인트, HTTP 메서드, Request/Response 구조

## Remote 모듈 아키텍처

Remote 관련 코드는 **5개의 독립 모듈**로 분리되어 있습니다:

```
:remote
├── :model        # Request/Response DTO 정의
│                 # 의존: core.common, kotlinx.serialization
│
├── :api          # Retrofit API 인터페이스
│                 # 의존: :remote:model, retrofit
│
├── :mapper       # DTO → Domain 매핑 확장 함수
│                 # 의존: :remote:model, :domain:model, core.utils
│
├── :datasource   # RemoteDataSource 구현체
│                 # 의존: :remote:api, :remote:mapper, :data:datasource, :domain:model, core.utils, Hilt
│
└── :network      # 네트워크 설정 및 DI 모듈
                  # 의존: :remote:datasource, :domain:repository, core.utils, core.auth, retrofit, okhttp, Hilt
                  # 역할: ApiModule, RemoteDataSourceModule, NetworkModule 등록
```

Data Layer 모듈:

```
:data
├── :datasource      # DataSource 인터페이스 (Remote + Local)
│                    # 의존: :domain:model, Hilt
│
└── :repositoryImpl  # Repository 구현체 + DI
                     # 의존: :domain:repository, :data:datasource, core.utils, Hilt
```

## 구현 순서 (9단계)

### 1단계: DTO 모델 (Request/Response)

**모듈**: `:remote:model`
**위치**: `remote/model/src/main/kotlin/ktc/cargo/driver/remote/model/{feature}/`
(단순한 경우 `remote/model/src/main/kotlin/ktc/cargo/driver/remote/model/` 에 직접)

**규칙**:
- `@Serializable` 어노테이션 필수
- **Response는 반드시 `BaseResponse()` 상속**
- **Request는 `BaseRequest()` 상속** (act_app 자동 포함)
- 접근 제한자 없음 (다른 모듈에서 참조 가능해야 함)
- API 필드가 snake_case면 `@SerialName` 사용

**참고 패턴**:
```kotlin
package ktc.cargo.driver.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ktc.cargo.driver.remote.model.base.BaseRequest
import ktc.cargo.driver.remote.model.base.BaseResponse

@Serializable
data class BlockRequest(
	val corp_seq: String,
	val mode: String,
) : BaseRequest()

@Serializable
data class BlockListResponse(
	val data: List<BlockItemResponse>,
) : BaseResponse() {

	@Serializable
	data class BlockItemResponse(
		@SerialName("corp_seq")
		val corpSeq: Int = 0,
		@SerialName("corp_name")
		val corpName: String = "",
	)
}
```

**참고 파일**:
- `remote/model/src/main/kotlin/ktc/cargo/driver/remote/model/base/BaseResponse.kt`
- `remote/model/src/main/kotlin/ktc/cargo/driver/remote/model/base/BaseRequest.kt`
- `remote/model/src/main/kotlin/ktc/cargo/driver/remote/model/BlockData.kt`
- `remote/model/src/main/kotlin/ktc/cargo/driver/remote/model/cargo/CargoListData.kt`

### 2단계: API 인터페이스

**모듈**: `:remote:api`
**위치**: `remote/api/src/main/kotlin/ktc/cargo/driver/remote/api/`

**규칙**:
- **기능별로 독립 파일** 생성: `{Feature}Api.kt`
- `interface` 사용 (접근 제한자 없음)
- HTTP 메서드 어노테이션 (`@GET`, `@POST`, `@PUT`, `@DELETE`)
- `suspend fun` 사용
- **반환 타입: `ApiResult<T>`** (T는 BaseResponse 하위 타입)
- `@Body` 어노테이션으로 Request 전달

**참고 패턴**:
```kotlin
package ktc.cargo.driver.remote.api

import ktc.cargo.driver.remote.model.BlockListRequest
import ktc.cargo.driver.remote.model.BlockListResponse
import ktc.cargo.driver.remote.model.BlockRequest
import ktc.cargo.driver.remote.model.base.ApiResult
import ktc.cargo.driver.remote.model.base.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface BlockApi {

	@POST("cargo/my/owblock")
	suspend fun updateBlocking(
		@Body request: BlockRequest,
	): ApiResult<BaseResponse>

	@POST("cargo/my/owblock/list")
	suspend fun getBlockHwajuList(
		@Body request: BlockListRequest,
	): ApiResult<BlockListResponse>
}
```

**참고 파일**:
- `remote/api/src/main/kotlin/ktc/cargo/driver/remote/api/BlockApi.kt`
- `remote/api/src/main/kotlin/ktc/cargo/driver/remote/api/AccountApi.kt`
- `remote/api/src/main/kotlin/ktc/cargo/driver/remote/api/LoginApi.kt`

### 3단계: Mapper (DTO → Domain)

**모듈**: `:remote:mapper`
**위치**: `remote/mapper/src/main/kotlin/ktc/cargo/driver/remote/mapper/`

**규칙**:
- **기능별로 독립 파일** 생성: `{Feature}Mapper.kt`
- 확장 함수로 작성 (접근 제한자 없음)
- 네이밍: `fun {Response}.to{DomainModel}(): DomainModel`
- 패키지: `ktc.cargo.driver.remote.mapper`

**참고 패턴**:
```kotlin
package ktc.cargo.driver.remote.mapper

import ktc.cargo.driver.domain.model.block.BlockedCorp
import ktc.cargo.driver.remote.model.corp.CorpModel

fun CorpModel.toBlockedCorp(): BlockedCorp {
	return BlockedCorp(
		seq = corp_seq,
		businessNumber = corp_num,
		ceoName = corp_ceo,
		name = corp_name ?: "",
	)
}
```

**참고 파일**:
- `remote/mapper/src/main/kotlin/ktc/cargo/driver/remote/mapper/BlockMapper.kt`
- `remote/mapper/src/main/kotlin/ktc/cargo/driver/remote/mapper/AccountMapper.kt`
- `remote/mapper/src/main/kotlin/ktc/cargo/driver/remote/mapper/UserInfoMapper.kt`

### 4단계: RemoteDataSource 인터페이스

**모듈**: `:data:datasource`
**위치**: `data/datasource/src/main/kotlin/ktc/cargo/driver/data/datasoure/remote/`

> **주의**: 패키지명이 `datasoure` (오타가 프로젝트 전체에 적용됨, 수정하지 않음)

**규칙**:
- `interface` (접근 제한자 없음)
- **Domain 모델 반환** (DTO 사용 금지)
- 조회 작업: `Flow<DomainModel>` 반환 + `onError` 파라미터
- 쓰기 작업: `suspend fun` + `onSuccess`/`onError` 파라미터
- 패키지: `ktc.cargo.driver.data.datasoure.remote`

**참고 패턴**:
```kotlin
package ktc.cargo.driver.data.datasoure.remote

import kotlinx.coroutines.flow.Flow
import ktc.cargo.driver.domain.model.MessageType
import ktc.cargo.driver.domain.model.block.BlockedCorp

interface BlockRemoteDataSource {

	fun getBlockedList(
		limit: Int = 100,
		offset: Int = 0,
		onError: (MessageType) -> Unit,
	): Flow<List<BlockedCorp>>

	suspend fun updateBlockStatus(
		corpSeq: String,
		isBlocked: Boolean,
		onSuccess: () -> Unit,
		onError: (MessageType) -> Unit,
	)
}
```

**참고 파일**:
- `data/datasource/src/main/kotlin/ktc/cargo/driver/data/datasoure/remote/BlockRemoteDataSource.kt`
- `data/datasource/src/main/kotlin/ktc/cargo/driver/data/datasoure/remote/AccountRemoteDataSource.kt`

### 5단계: RemoteDataSource 구현

**모듈**: `:remote:datasource`
**위치**: `remote/datasource/src/main/kotlin/ktc/cargo/driver/remote/datasource/`

**규칙**:
- `class` + `@Inject constructor` (접근 제한자 없음)
- API 인터페이스 주입
- 패키지: `ktc.cargo.driver.remote.datasource`

**Flow 반환 (조회 작업)**:
- `flow { }` 블록 사용
- `.suspendOnFailureWithErrorHandling(onError)` 에러 처리
- `.suspendOnSuccess { emit(response.to{Domain}()) }` 성공 처리
- `.catch { }` 로 예외 로깅

**suspend 함수 (쓰기 작업)**:
- `try-catch` 블록 사용
- 성공 시 `onSuccess()` 호출

**참고 패턴**:
```kotlin
package ktc.cargo.driver.remote.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import ktc.cargo.driver.data.datasoure.remote.BlockRemoteDataSource
import ktc.cargo.driver.domain.model.MessageType
import ktc.cargo.driver.domain.model.block.BlockedCorp
import ktc.cargo.driver.remote.api.BlockApi
import ktc.cargo.driver.remote.datasource.apiResult.suspendOnFailureWithErrorHandling
import ktc.cargo.driver.remote.datasource.apiResult.suspendOnSuccess
import ktc.cargo.driver.remote.mapper.toBlockedCorp
import ktc.cargo.driver.remote.model.BlockListRequest
import ktc.cargo.driver.remote.model.BlockRequest
import ktc.cargo.driver.utils.etc.Logger
import javax.inject.Inject

class BlockRemoteDataSourceImpl @Inject constructor(
	private val api: BlockApi,
) : BlockRemoteDataSource {

	// Flow 반환 (조회)
	override fun getBlockedList(
		limit: Int,
		offset: Int,
		onError: (MessageType) -> Unit,
	): Flow<List<BlockedCorp>> = flow {
		val request = BlockListRequest(
			limit = limit,
			offset = offset,
		)

		api.getBlockHwajuList(request)
			.suspendOnFailureWithErrorHandling(onError)
			.suspendOnSuccess {
				emit(response.data.map { it.toBlockedCorp() })
			}
	}.catch { exception ->
		Logger.e("차단 목록 조회 실패: ${exception.message}")
		onError(MessageType.Message("차단 목록 조회에 실패했습니다."))
	}

	// suspend 함수 (쓰기)
	override suspend fun updateBlockStatus(
		corpSeq: String,
		isBlocked: Boolean,
		onSuccess: () -> Unit,
		onError: (MessageType) -> Unit,
	) {
		try {
			val request = BlockRequest(
				corp_seq = corpSeq,
				mode = if (isBlocked) "I" else "D",
			)

			api.updateBlocking(request)
				.suspendOnFailureWithErrorHandling(onError)
				.suspendOnSuccess {
					onSuccess()
				}
		} catch (exception: Exception) {
			Logger.e("차단 상태 업데이트 실패: ${exception.message}")
			onError(MessageType.Message("차단 상태 업데이트에 실패했습니다."))
		}
	}
}
```

**참고 파일**:
- `remote/datasource/src/main/kotlin/ktc/cargo/driver/remote/datasource/BlockRemoteDataSourceImpl.kt`
- `remote/datasource/src/main/kotlin/ktc/cargo/driver/remote/datasource/AccountRemoteDataSourceImpl.kt`

### 6단계: ApiModule에 등록

**모듈**: `:remote:network`
**위치**: `remote/network/src/main/kotlin/ktc/cargo/driver/data/network/di/ApiModule.kt`

기존 파일에 **추가**:
```kotlin
@Provides
@Singleton
fun provide{Feature}Api(
	retrofit: Retrofit,
): {Feature}Api = retrofit.create({Feature}Api::class.java)
```

### 7단계: RemoteDataSourceModule에 등록

**모듈**: `:remote:network`
**위치**: `remote/network/src/main/kotlin/ktc/cargo/driver/data/network/di/RemoteDataSourceModule.kt`

기존 파일에 **추가**:
```kotlin
@Binds
abstract fun bind{Feature}DataSource(
	impl: {Feature}RemoteDataSourceImpl,
): {Feature}RemoteDataSource
```

### 8단계: RepositoryImpl 구현

**모듈**: `:data:repositoryImpl`
**위치**: `data/repositoryImpl/src/main/kotlin/ktc/cargo/driver/data/repositoryImpl/`

**규칙**:
- `internal class` + `@Inject constructor`
- DataSource를 주입받아 Domain Repository 구현
- 비즈니스 로직 없음 (단순 DataSource 위임)
- 패키지: `ktc.cargo.driver.data.repositoryImpl`

**참고 패턴**:
```kotlin
package ktc.cargo.driver.data.repositoryImpl

import kotlinx.coroutines.flow.Flow
import ktc.cargo.driver.data.datasoure.remote.BlockRemoteDataSource
import ktc.cargo.driver.domain.model.MessageType
import ktc.cargo.driver.domain.model.block.BlockedCorp
import ktc.cargo.driver.domain.repository.BlockRepository
import javax.inject.Inject

internal class BlockRepositoryImpl @Inject constructor(
	private val blockRemoteDataSource: BlockRemoteDataSource,
) : BlockRepository {

	override fun getBlockedList(
		limit: Int,
		offset: Int,
		onError: (MessageType) -> Unit,
	): Flow<List<BlockedCorp>> {
		return blockRemoteDataSource.getBlockedList(
			limit = limit,
			offset = offset,
			onError = onError,
		)
	}

	override suspend fun updateBlockStatus(
		corpSeq: Int,
		isBlocked: Boolean,
		onSuccess: () -> Unit,
		onError: (MessageType) -> Unit,
	) {
		blockRemoteDataSource.updateBlockStatus(
			corpSeq = corpSeq.toString(),
			isBlocked = isBlocked,
			onSuccess = onSuccess,
			onError = onError,
		)
	}
}
```

**참고 파일**:
- `data/repositoryImpl/src/main/kotlin/ktc/cargo/driver/data/repositoryImpl/BlockRepositoryImpl.kt`
- `data/repositoryImpl/src/main/kotlin/ktc/cargo/driver/data/repositoryImpl/AccountRepositoryImpl.kt`

### 9단계: RepositoryModule에 등록

**모듈**: `:data:repositoryImpl`
**위치**: `data/repositoryImpl/src/main/kotlin/ktc/cargo/driver/data/repositoryImpl/di/RepositoryModule.kt`

기존 파일에 **추가**:
```kotlin
@Binds
abstract fun binds{Feature}Repository(
	repositoryImpl: {Feature}RepositoryImpl,
): {Feature}Repository
```

## 파일 위치 요약

| 단계 | 모듈 | 파일 위치 | 네이밍 |
|------|------|-----------|--------|
| 1. DTO | `:remote:model` | `remote/model/src/main/kotlin/ktc/cargo/driver/remote/model/` | `{Feature}Data.kt` |
| 2. API | `:remote:api` | `remote/api/src/main/kotlin/ktc/cargo/driver/remote/api/` | `{Feature}Api.kt` |
| 3. Mapper | `:remote:mapper` | `remote/mapper/src/main/kotlin/ktc/cargo/driver/remote/mapper/` | `{Feature}Mapper.kt` |
| 4. DataSource IF | `:data:datasource` | `data/datasource/src/main/kotlin/ktc/cargo/driver/data/datasoure/remote/` | `{Feature}RemoteDataSource.kt` |
| 5. DataSource Impl | `:remote:datasource` | `remote/datasource/src/main/kotlin/ktc/cargo/driver/remote/datasource/` | `{Feature}RemoteDataSourceImpl.kt` |
| 6. ApiModule | `:remote:network` | `remote/network/src/main/kotlin/ktc/cargo/driver/data/network/di/ApiModule.kt` | 기존 파일에 추가 |
| 7. DS Module | `:remote:network` | `remote/network/src/main/kotlin/ktc/cargo/driver/data/network/di/RemoteDataSourceModule.kt` | 기존 파일에 추가 |
| 8. RepoImpl | `:data:repositoryImpl` | `data/repositoryImpl/src/main/kotlin/ktc/cargo/driver/data/repositoryImpl/` | `{Feature}RepositoryImpl.kt` |
| 9. Repo Module | `:data:repositoryImpl` | `data/repositoryImpl/src/main/kotlin/ktc/cargo/driver/data/repositoryImpl/di/RepositoryModule.kt` | 기존 파일에 추가 |

## 접근 제한자 규칙

| 대상 | 접근 제한자 | 이유 |
|------|-------------|------|
| DTO (Request/Response) | 없음 (public) | 여러 모듈에서 참조 |
| API Interface | 없음 (public) | `:remote:datasource`에서 참조 |
| Mapper 함수 | 없음 (public) | `:remote:datasource`에서 참조 |
| RemoteDataSource Interface | 없음 (public) | `:data:repositoryImpl`에서 참조 |
| RemoteDataSourceImpl | 없음 (public) | `:remote:network`의 DI 모듈에서 참조 |
| RepositoryImpl | `internal` | `:data:repositoryImpl` 모듈 내부에서만 사용 |

## 에러 처리 패턴

- **ApiResult** 기반: `suspendOnSuccess` / `suspendOnFailureWithErrorHandling`
- **Flow 반환**: `.catch { Logger.e(); onError() }`
- **suspend 함수**: `try-catch` 블록 + `Logger.e()` + `onError()`
- import 경로: `ktc.cargo.driver.remote.datasource.apiResult.suspendOnSuccess`
- import 경로: `ktc.cargo.driver.remote.datasource.apiResult.suspendOnFailureWithErrorHandling`
- Logger: `ktc.cargo.driver.utils.etc.Logger`

## 빌드 검증

구현 완료 후 다음 명령으로 컴파일 확인:
- 순수 Kotlin 모듈 (`domain:model`, `domain:repository`, `domain:usecase`): `compileKotlin`
- Android 모듈 (나머지 전부): `compileDebugKotlin`

## 체크리스트

구현 완료 후 확인:
- [ ] Response가 `BaseResponse()` 상속하는지
- [ ] Request가 `BaseRequest()` 상속하는지
- [ ] API 반환 타입이 `ApiResult<T>`인지
- [ ] API 인터페이스가 **기능별 독립 파일**인지 (`{Feature}Api.kt`)
- [ ] Mapper가 **기능별 독립 파일**인지 (`{Feature}Mapper.kt`)
- [ ] Mapper가 DTO → Domain 변환만 하는지
- [ ] DataSource 인터페이스 패키지가 `datasoure` (오타 유지)인지
- [ ] RemoteDataSourceImpl에 `@Inject constructor` 있는지
- [ ] RepositoryImpl이 `internal class`인지
- [ ] ApiModule에 `@Provides @Singleton` 등록했는지
- [ ] RemoteDataSourceModule에 `@Binds` 등록했는지
- [ ] RepositoryModule에 `@Binds` 등록했는지
