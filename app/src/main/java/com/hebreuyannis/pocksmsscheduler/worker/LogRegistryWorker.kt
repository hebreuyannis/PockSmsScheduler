package com.hebreuyannis.pocksmsscheduler.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hebreuyannis.pocksmsscheduler.R
import com.hebreuyannis.pocksmsscheduler.model.TransactionDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


const val STATUS = "STATUS"
const val TRANSACTION_ID = "TRANSACTION_ID"

class LogRegistryWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        println("LogRegistryWorker start")
        val id = inputData.getString(TRANSACTION_ID)
            ?: throw NullPointerException("transaction_id must not be null")
        val status = inputData.getInt(STATUS, 2)
        val job = async { updateTransactionLog(id, status = status) }
        job.await()
        Result.success()
    }


    private fun updateTransactionLog(idTransaction: String, status: Int) {
        val dataSource = TransactionDatabase.getInstance(applicationContext).transactionDatabaseDao
        val transaction = dataSource.getTransactionById(idTransaction)
            ?: throw NullPointerException("Transaction must not be null")
        val updatedTransaction =
            transaction.copy(dateUpdate = System.currentTimeMillis(), status = status)
        dataSource.updateTransactionLog(
            updatedTransaction.id,
            updatedTransaction.dateUpdate,
            updatedTransaction.status
        )
    }

}

// Name of Notification Channel for verbose notifications of background work
@JvmField
val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever work starts"

@JvmField
val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1


fun makeStatusNotification(message: String, context: Context) {

    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        // Add the channel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_ad_units)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Show the notification
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}



