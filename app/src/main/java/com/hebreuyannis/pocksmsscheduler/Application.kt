package com.hebreuyannis.pocksmsscheduler

import android.app.Application
import com.github.tamir7.contacts.Contacts

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        Contacts.initialize(this)
    }
}