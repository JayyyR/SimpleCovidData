package com.joeracosta.covidtracker

import com.joeracosta.covidtracker.data.LastUpdatedData
import com.joeracosta.covidtracker.data.State
import org.junit.Rule

class TestLastUpdatedData: LastUpdatedData {
    
    var storedLastUpdatedTime = 0L
    var storedSelectedUSState: State? = null
    var storedAmountOfDatsAgoToShow: Int? = null

    override fun getLastUpdatedTime(): Long {
        return storedLastUpdatedTime
    }

    override fun setLastUpdatedTime(lastUpdatedTime: Long) {
        storedLastUpdatedTime = lastUpdatedTime
    }

    override fun setSelectedUSState(state: State) {
        storedSelectedUSState = state
    }

    override fun getSelectedUSState(): State? {
        return storedSelectedUSState
    }

    override fun getAmountOfDaysAgoToShow(): Int? {
        return storedAmountOfDatsAgoToShow
    }

    override fun setAmountOfDaysAgoToShow(fromDate: Int) {
        storedAmountOfDatsAgoToShow = fromDate
    }
}