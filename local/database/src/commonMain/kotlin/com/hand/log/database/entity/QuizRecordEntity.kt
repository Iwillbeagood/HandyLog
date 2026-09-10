package com.hand.log.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hand.log.domain.model.preflop.QuizRecordQuestion

@Entity(tableName = "quiz_records")
data class QuizRecordEntity(
	@PrimaryKey val id: String,
	val quizType: String,
	val stack: String = "",
	val score: Int,
	val total: Int,
	val avgResponseMs: Long,
	val bestStreak: Int,
	val playedAt: Long,
	val questions: List<QuizRecordQuestion> = emptyList(),
)
