package com.joeracosta.covidtracker.data

import android.content.SharedPreferences
import java.util.*

interface LastUpdatedData {
    fun getLastUpdatedTime(): Long
    fun setLastUpdatedTime(lastUpdatedTime: Long)
    fun setSelectedUSState(state: State)
    fun getSelectedUSState(): State?
    fun getAmountOfDaysAgoToShow(): Int?
    fun setAmountOfDaysAgoToShow(fromDate: Int)
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

    override fun getAmountOfDaysAgoToShow(): Int? {
        val timeFrom = sharedPreferences.getInt(SELECTED_AMOUNT_OF_DAYS_AGO_TO_SHOW, -1)
        return if (timeFrom != -1) {
            timeFrom
        } else {
            null
        }
    }

    override fun setAmountOfDaysAgoToShow(amountOfDaysAgoToShow: Int) {
        sharedPreferences.edit().putInt(SELECTED_AMOUNT_OF_DAYS_AGO_TO_SHOW, amountOfDaysAgoToShow).apply()
    }

    companion object {
        const val LAST_UPDATED_TIME = "com.joeracosta.last_update_time"
        const val SELECTED_STATE = "com.joeracosta.selected_state"
        const val SELECTED_AMOUNT_OF_DAYS_AGO_TO_SHOW = "com.joeracosta.selected_time_frame"
    }
}