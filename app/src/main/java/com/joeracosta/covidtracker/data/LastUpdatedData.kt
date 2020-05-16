package com.joeracosta.covidtracker.data

import android.content.SharedPreferences
import java.util.*

interface LastUpdatedData {
    fun getLastUpdatedTime(): Long
    fun setLastUpdatedTime(lastUpdatedTime: Long)
}

class LastUpdatedDataConcrete(
    private val sharedPreferences: SharedPreferences
): LastUpdatedData {

    override fun getLastUpdatedTime(): Long {
        return sharedPreferences.getLong(LAST_UPDATED_TIME, 0)
    }

    override fun setLastUpdatedTime(lastUpdatedTime: Long) {
        sharedPreferences.edit().putLong(LAST_UPDATED_TIME, lastUpdatedTime).apply()
    }

    companion object {
        const val LAST_UPDATED_TIME = "com.joeracosta.last_update_time"
    }
}