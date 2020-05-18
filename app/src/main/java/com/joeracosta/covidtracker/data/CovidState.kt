package com.joeracosta.covidtracker.data

import java.util.*

data class CovidState(
    val selectedUsaState: State? = null,
    val amountOfDaysAgoToShow: Int? = null,
    val chartedData: List<CovidData>? = null,
    val updatingData: Boolean? = null
)