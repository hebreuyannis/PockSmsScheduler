package com.hebreuyannis.pocksmsscheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hebreuyannis.pocksmsscheduler.worker.makeStatusNotification


class BootCompleteReceiver : BroadcastReceiver() {
    private val TAG = BootCompleteReceiver::class.java.simpleName
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "BootCompleteReceiver ${intent?.action}")
        makeStatusNotification(
            "Your smartphone has been reboot please check the sending status",
            context
        )
    }
}
