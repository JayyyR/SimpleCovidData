package com.joeracosta.covidtracker

import androidx.databinding.BindingAdapter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addToComposite(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

@BindingAdapter("xAxisFormatter")
fun LineChart.setXAxisLabelFormatter(xAxisFormatter: (Float) -> String) {
    // the labels that should be drawn on the XAxis
    val formatter: ValueFormatter =
        object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return xAxisFormatter.invoke(value)
            }
        }
    val xAxis = this.xAxis
    xAxis?.valueFormatter = formatter
}