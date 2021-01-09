package com.joeracosta.covidtracker.data

enum class DataToPlot(val id: Int) {
    POSITIVE_CASE_RATE(0),
    CURRENT_HOSPITALIZATIONS(1),
    NEW_VACCINATIONS(2),
    TOTAL_VACCINATIONS(3)
}