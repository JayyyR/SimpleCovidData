package com.joeracosta.covidtracker.data

import android.content.SharedPreferences
import java.util.*

interface LastUpdatedData {
    fun getLastUpdatedTime(): Long
    fun setLastUpdatedTime(lastUpdatedTime: Long)
    fun setSelectedUSState(state: State)
    fun getSelectedUSState(): State?
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

    override fun setSelectedUSState(state: State) {
        sharedPreferences.edit().putString(SELECTED_STATE, state.postalCode).apply()
    }

    override fun getSelectedUSState(): State? {
        val postalCode = sharedPreferences.getString(SELECTED_STATE, "")
        return State.values().find { it.postalCode == postalCode }
    }

    companion object {
        const val LAST_UPDATED_TIME = "com.joeracosta.last_update_time"
        const val SELECTED_STATE = "com.joeracosta.selected_state"
    }
}