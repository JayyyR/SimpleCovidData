package com.joeracosta.covidtracker.data

data class CovidState(
    val selectedUsaLocation: Location? = null,
    val amountOfDaysAgoToShow: Int? = null,
    val chartedData: List<CovidData>? = null,
    val updatingData: Boolean? = null,
    val dataToPlot: DataToPlot? = null
)