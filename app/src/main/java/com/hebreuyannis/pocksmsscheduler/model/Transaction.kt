package com.hebreuyannis.pocksmsscheduler.model

import androidx.work.Data
import androidx.work.workDataOf
import java.util.*

data class Transaction(
    val recipient: String,
    val recipientNumber: String,
    val contentDescription: String,
    val targetDate: Long
) : IWorker {
    override val id: String = UUID.randomUUID().toString()
    override fun convertToWorkData(): Data {
        return workDataOf(
            ID to id,
            NUMERO to recipientNumber,
            RECIPIENT to recipient,
            CONTENT to contentDescription
        )
    }

    fun geTimeMillisSinceNow() = targetDate - System.currentTimeMillis()

    companion object {
        const val RECIPIENT = "RECIPIENT"
        const val NUMERO = "NUMERO"
        const val CONTENT = "TEXT"
        const val ID = "ID"
    }
}

internal fun Transaction.toEntity(): TransactionLogEntity {
    return TransactionLogEntity(
        idTransaction = id,
        recipient = recipient,
        recipientNumber = recipientNumber,
        status = -1,
        targetDate = targetDate

    )
}