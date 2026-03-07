package com.hand.log.domain.model.etc.error

sealed interface NoticeErrorType {
	data class Snackbar(val message: String) : NoticeErrorType
	data class Dialog(val message: String) : NoticeErrorType
	data class FullDialog(val message: String) : NoticeErrorType
	data class OneButtonDialog(val message: String) : NoticeErrorType
}
