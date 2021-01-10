package com.joeracosta.covidtracker

import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import com.joeracosta.covidtracker.data.Location
import com.joeracosta.covidtracker.viewmodel.CovidViewModel
import com.joeracosta.covidtracker.data.DataToPlot
import org.mockito.Mockito

class CovidRobot {

    private val lastUpdatedData = TestLastUpdatedData()

    private var viewModel = CovidViewModel(
        covidTrackingProjectApi = TestCovidApi(),
        ourWorldInDataApi = TestOurWorldInDataApi(),
        covidDataDao = TestCovidDao(),
        lastUpdatedData = lastUpdatedData,
        stringGetter = TestStringGetter(),
        appResources = Mockito.mock(Resources::class.java)
    )


    fun setSelectedUSState(location: Location) = apply {
        viewModel.setSelectedUSState(location)
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

    fun assertSelectedState(selectedLocation: Location) = apply {
        assertThat(viewModel.stateSubject.value?.selectedUsaLocation).isEqualTo(selectedLocation)
    }

    fun assertSelectedStateIsStored(selectedLocation: Location) = apply {
        assertThat(lastUpdatedData.storedSelectedUSLocation).isEqualTo(selectedLocation)
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