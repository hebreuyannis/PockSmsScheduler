package com.hebreuyannis.pocksmsscheduler.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_Log")
data class TransactionLogEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "id_transaction")
    val idTransaction: String,
    @ColumnInfo(name = "date_create")
    val dateCreate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "date_update")
    val dateUpdate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "target_date")
    val targetDate: Long,
    @ColumnInfo(name = "status")
    val status: Int,//-1: untreated,0:Success,1 & above:error
    val recipient: String,
    val recipientNumber: String
)