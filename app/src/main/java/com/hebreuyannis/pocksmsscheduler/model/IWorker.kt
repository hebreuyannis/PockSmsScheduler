package com.hebreuyannis.pocksmsscheduler.model

import androidx.work.Data

interface IWorker {
    val id: String
    fun convertToWorkData(): Data
}