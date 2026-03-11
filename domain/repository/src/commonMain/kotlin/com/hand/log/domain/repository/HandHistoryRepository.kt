package com.hand.log.domain.repository

import com.hand.log.domain.model.HandHistory
import kotlinx.coroutines.flow.Flow

interface HandHistoryRepository {

	/**
	 * 전체 핸드 히스토리 목록 조회 (최신순)
	 *
	 * @return 저장된 핸드 히스토리 목록의 Flow
	 */
	fun observeAllHands(): Flow<List<HandHistory>>

	/**
	 * 기간별 핸드 히스토리 조회
	 *
	 * @param startDate 조회 시작 날짜 (timestamp)
	 * @param endDate 조회 종료 날짜 (timestamp)
	 * @return 해당 기간의 핸드 히스토리 목록 Flow
	 */
	fun observeHandsByDateRange(
		startDate: Long,
		endDate: Long,
	): Flow<List<HandHistory>>

	/**
	 * 특정 핸드 히스토리 상세 조회
	 *
	 * @param handId 조회할 핸드 ID
	 * @return 핸드 히스토리 (없으면 null)
	 */
	suspend fun getHandById(handId: String): HandHistory?

	/**
	 * 핸드 히스토리 저장 (플레이어, 액션, 커뮤니티 카드 포함)
	 *
	 * @param handHistory 저장할 핸드 히스토리
	 * @param onSuccess 저장 성공 콜백
	 * @param onError 에러 핸들러
	 */
	suspend fun saveHandHistory(
		handHistory: HandHistory,
		onSuccess: () -> Unit,
	)

	/**
	 * 핸드 히스토리 삭제 (관련 플레이어, 액션, 커뮤니티 카드 포함)
	 *
	 * @param handId 삭제할 핸드 ID
	 * @param onSuccess 삭제 성공 콜백
	 * @param onError 에러 핸들러
	 */
	suspend fun deleteHandHistory(
		handId: String,
		onSuccess: () -> Unit,
	)

	/**
	 * 저장된 핸드 히스토리 총 개수 조회
	 *
	 * @return 핸드 히스토리 개수
	 */
	suspend fun getHandCount(): Int
}
