package com.joeracosta.covidtracker

import com.joeracosta.covidtracker.data.DataToPlot
import com.joeracosta.covidtracker.data.LastUpdatedData
import com.joeracosta.covidtracker.data.Location

class TestLastUpdatedData: LastUpdatedData {
    
    var storedLastUpdatedTime = 0L
    var storedSelectedUSLocation: Location? = null
    var storedAmountOfDatsAgoToShow: Int? = null
    var storedDataToPlot: DataToPlot? = null

    override fun getLastUpdatedTime(): Long {
        return storedLastUpdatedTime
    }

    override fun setLastUpdatedTime(lastUpdatedTime: Long) {
        storedLastUpdatedTime = lastUpdatedTime
    }

    override fun setSelectedUSState(location: Location) {
        storedSelectedUSLocation = location
    }

    override fun getSelectedUSState(): Location? {
        return storedSelectedUSLocation
    }

    override fun getAmountOfDaysAgoToShow(): Int? {
        return storedAmountOfDatsAgoToShow
    }

    override fun setAmountOfDaysAgoToShow(fromDate: Int) {
        storedAmountOfDatsAgoToShow = fromDate
    }

    override fun getDataToPlot(): DataToPlot? {
        return storedDataToPlot
    }

    override fun setDataToPlot(dataToPlot: DataToPlot) {
        storedDataToPlot = dataToPlot
    }
}