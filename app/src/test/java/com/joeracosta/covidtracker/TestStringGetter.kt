package com.joeracosta.covidtracker

import com.joeracosta.covidtracker.data.StringGetter

class TestStringGetter: StringGetter {
    override fun getString(id: Int): String {
        return ""
    }
}