package com.joeracosta.covidtracker.data

import android.content.SharedPreferences

interface LastUpdatedData {
    fun getLastUpdatedTime(): Long
    fun setLastUpdatedTime(lastUpdatedTime: Long)
    fun setSelectedUSState(state: State)
    fun getSelectedUSState(): State?
    fun getAmountOfDaysAgoToShow(): Int?
    fun setAmountOfDaysAgoToShow(fromDate: Int)
    fun getDataToPlot(): DataToPlot?
    fun setDataToPlot(dataToPlot: DataToPlot)
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

    override fun setAmountOfDaysAgoToShow(fromDate: Int) {
        sharedPreferences.edit().putInt(SELECTED_AMOUNT_OF_DAYS_AGO_TO_SHOW, fromDate).apply()
    }

    override fun getDataToPlot(): DataToPlot? {
        val dataToPlotId = sharedPreferences.getInt(SELECTED_DATA_TO_PLOT, -1)
        return if (dataToPlotId != -1) {
            DataToPlot.values().find { it.id == dataToPlotId }
        } else {
            null
        }
    }

    override fun setDataToPlot(dataToPlot: DataToPlot) {
        sharedPreferences.edit().putInt(SELECTED_DATA_TO_PLOT, dataToPlot.id).apply()
    }

    companion object {
        const val LAST_UPDATED_TIME = "com.joeracosta.last_update_time"
        const val SELECTED_STATE = "com.joeracosta.selected_state"
        const val SELECTED_AMOUNT_OF_DAYS_AGO_TO_SHOW = "com.joeracosta.selected_time_frame"
        const val SELECTED_DATA_TO_PLOT = "com.joeracosta.selected_data_to_plot"
    }
}