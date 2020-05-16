package com.joeracosta.covidtracker.data

import java.util.*

data class CovidState(
    val selectedUsaState: State? = null,
    val showDataFromDate: Date? = null,
    val chartedData: List<CovidData>? = null,
    val updatingData: Boolean? = null
)