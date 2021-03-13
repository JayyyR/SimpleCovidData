package com.joeracosta.covidtracker.data

enum class DataToPlot(val id: Int) {
    NEW_CASES(0),
    NEW_DEATHS(1),
    NEW_VACCINATIONS(2),
    TOTAL_VACCINATIONS(3),
    PERCENT_VACCINATED(4)
}