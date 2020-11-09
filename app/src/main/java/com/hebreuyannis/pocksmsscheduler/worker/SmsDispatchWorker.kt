
package com.hebreuyannis.pocksmsscheduler.worker

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.hebreuyannis.pocksmsscheduler.PockSmsManager
import com.hebreuyannis.pocksmsscheduler.model.Transaction
import com.hebreuyannis.pocksmsscheduler.model.TransactionDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val SUCCESS = 0

class SmsDispatchWorker(context: Context, params: WorkerParameters) :
    ListenableWorker(context, params) {

    private val job = Job()
    private val dispatcher = Dispatchers.IO

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<Result> ->
            var status = -1
            val transactionId = inputData.getString(Transaction.ID)
                ?: throw NullPointerException("transaction id must not be null")
            val number = inputData.getString(Transaction.NUMERO)
                ?: throw NullPointerException("transaction number must not be null")
            val content = inputData.getString(Transaction.CONTENT)
                ?: throw NullPointerException("transaction content must not be null")
            val smsManager = PockSmsManager(applicationContext)
            val callbackSms = object : PockSmsManager.OnDispatchListener {
                override fun dispatchSuccessful() {
                    println("SmsDispatchWorker: sms was send successfully ")
                    completer.set(
                        Result.success(
                            workDataOf(
                                TRANSACTION_ID to transactionId,
                                STATUS to SUCCESS
                            )
                        )
                    )
                }

                override fun dispatchError(reason: Int) {
                    println("SmsDispatchWorker: Failed to send sms, type error: $reason")
                    makeStatusNotification("sms not send ", applicationContext)
                    status = reason
                    completer.set(
                        Result.success(
                            workDataOf(
                                TRANSACTION_ID to transactionId,
                                STATUS to reason
                            )
                        )
                    )
                }
            }

            val coroutineScope = CoroutineScope(dispatcher + job)
            coroutineScope.launch {
                try {
                    val isNotOutdated = checkTransaction(transactionId)
                    if (isNotOutdated) {
                        smsManager.sendStrictSMS(number, content, callbackSms)
                    } else {
                        makeStatusNotification("sms not send ", applicationContext)
                        completer.set(
                            Result.success(
                                workDataOf(
                                    TRANSACTION_ID to transactionId,
                                    STATUS to status
                                )
                            )
                        )
                    }
                } catch (e: Exception) {
                    completer.set(Result.failure())
                }
            }
            return@getFuture callbackSms
        }
    }

    private fun checkTransaction(idTransaction: String): Boolean {
        val dataSource = TransactionDatabase.getInstance(applicationContext).transactionDatabaseDao
        val transaction = dataSource.getTransactionById(idTransaction)
            ?: throw NullPointerException("Transaction must not be null")
        val targetDate = transaction.targetDate + 3_600_000L //60minute
        return targetDate >= System.currentTimeMillis()
    }
}