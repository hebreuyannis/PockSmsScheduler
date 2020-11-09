package com.hebreuyannis.pocksmsscheduler

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager


class PockSmsManager(val context: Context) {

    companion object {
        const val SMS_SENT = "com.hebreuyannis.pocksmsscheduler.SMS_SENT"
    }

    private var _dispatchCallback: OnDispatchListener? = null

    interface OnDispatchListener {
        fun dispatchSuccessful()
        fun dispatchError(reason: Int)
    }

    fun sendStrictSMS(
        recipientNumber: String,
        contentMessage: String,
        callback: OnDispatchListener
    ) {
        _dispatchCallback = callback
        processSMS(contentMessage, recipientNumber)
    }

    private fun processSMS(msg: String, number: String) {
        val sentPI = PendingIntent.getBroadcast(
            context, 0,
            Intent(SMS_SENT), 0
        )

        val senderBroadcast: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        context.unregisterReceiver(this)
                        _dispatchCallback?.dispatchSuccessful()
                    }
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                        context.unregisterReceiver(this)
                        _dispatchCallback?.dispatchError(SmsManager.RESULT_ERROR_GENERIC_FAILURE)
                    }
                    SmsManager.RESULT_ERROR_NO_SERVICE -> {
                        context.unregisterReceiver(this)
                        _dispatchCallback?.dispatchError(SmsManager.RESULT_ERROR_NO_SERVICE)
                    }
                    SmsManager.RESULT_ERROR_NULL_PDU -> {
                        context.unregisterReceiver(this)
                        _dispatchCallback?.dispatchError(SmsManager.RESULT_ERROR_NULL_PDU)
                    }
                    SmsManager.RESULT_ERROR_RADIO_OFF -> {
                        context.unregisterReceiver(this)
                        _dispatchCallback?.dispatchError(SmsManager.RESULT_ERROR_NO_SERVICE)
                    }
                }
            }
        }
        context.registerReceiver(senderBroadcast, IntentFilter(SMS_SENT))
        val sms = SmsManager.getDefault()
        sms.sendTextMessage(number, null, msg, sentPI, null)
    }
}