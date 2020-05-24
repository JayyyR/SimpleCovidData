package com.joeracosta.covidtracker

import com.google.common.truth.Truth.assertThat
import com.joeracosta.covidtracker.data.State
import com.joeracosta.covidtracker.viewmodel.CovidViewModel
import com.joeracosta.covidtracker.BaseObservableViewModel
import com.joeracosta.covidtracker.data.DataToPlot

class CovidRobot {

    private val lastUpdatedData = TestLastUpdatedData()

    private var viewModel = CovidViewModel(
        covidDataApi = TestCovidApi(),
        covidDataDao = TestCovidDao(),
        lastUpdatedData = lastUpdatedData,
        stringGetter = TestStringGetter()
    )


    fun setSelectedUSState(state: State) = apply {
        viewModel.setSelectedUSState(state)
    }

    fun setSelectedTimeFrame(amountOfDaysAgoToShow: Int) = apply {
        viewModel.setSelectedTimeFrame(amountOfDaysAgoToShow)
    }

    fun setDatToPlot(dataToPlot: DataToPlot) = apply {
        viewModel.setDataToPlot(dataToPlot)
    }

    fun forceRefresh() = apply {
        viewModel.refreshData()
    }

    fun assertSelectedDataToPlot(selectedDataToPlot: DataToPlot) = apply {
        assertThat(viewModel.stateSubject.value?.dataToPlot).isEqualTo(selectedDataToPlot)
    }

    fun assertStoredSelectedDataToPlot(selectedDataToPlot: DataToPlot) = apply {
        assertThat(lastUpdatedData.storedDataToPlot).isEqualTo(selectedDataToPlot)
    }

    fun assertSelectedState(selectedState: State) = apply {
        assertThat(viewModel.stateSubject.value?.selectedUsaState).isEqualTo(selectedState)
    }

    fun assertSelectedStateIsStored(selectedState: State) = apply {
        assertThat(lastUpdatedData.storedSelectedUSState).isEqualTo(selectedState)
    }

    fun assertAmountOfDaysAgoToShow(amountOfDaysAgoToShow: Int) = apply {
        assertThat(viewModel.stateSubject.value?.amountOfDaysAgoToShow).isEqualTo(amountOfDaysAgoToShow)
    }

    fun assertAmountOfDaysAgoToShowIsStored(amountOfDaysAgoToShow: Int) = apply {
        assertThat(lastUpdatedData.storedAmountOfDatsAgoToShow).isEqualTo(amountOfDaysAgoToShow)
    }

    fun assertLastUpdatedTimeWasLessThanAMinuteAgo() = apply {
        val lastUpdatedTimePlusAMin = lastUpdatedData.getLastUpdatedTime() + MIN_MILLIS
        val currentTime = System.currentTimeMillis()
        val updatedLessThanAMinuteAgo = lastUpdatedTimePlusAMin > currentTime

        assertThat(updatedLessThanAMinuteAgo).isTrue()
    }

    companion object {
        const val MIN_MILLIS = 60 * 1000L
    }
}