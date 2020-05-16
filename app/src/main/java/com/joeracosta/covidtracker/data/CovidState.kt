package com.joeracosta.covidtracker.data

data class CovidState(
    val currentState: State? = null,
    val listOfData: List<CovidData>? = null,
    val updatingData: Boolean? = null
)