package com.hebreuyannis.pocksmsscheduler.model

data class TransactionPresentation(
    val dateCreate: Long,
    val dateUpdate: Long,
    val status: Int,
    val name: String
)

internal fun TransactionLogEntity.toPresentation(): TransactionPresentation {
    return TransactionPresentation(dateCreate, dateUpdate, status, recipient)
}