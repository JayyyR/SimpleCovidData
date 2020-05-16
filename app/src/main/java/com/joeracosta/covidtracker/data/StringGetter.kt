package com.joeracosta.covidtracker.data

import android.content.res.Resources

interface StringGetter {
    fun getString(id: Int): String
}

class StringGetterConcrete(
    private val appResources: Resources
): StringGetter {
    override fun getString(id: Int): String {
        return appResources.getString(id)
    }
}