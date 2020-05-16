package com.joeracosta.covidtracker.view

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.joeracosta.covidtracker.CovidApp
import com.joeracosta.covidtracker.R
import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.CovidData
import com.joeracosta.covidtracker.data.CovidDataApi
import com.joeracosta.covidtracker.databinding.ActivityMainBinding
import com.joeracosta.covidtracker.viewmodel.CovidViewModel
import io.reactivex.disposables.CompositeDisposable

class CovidActivity : AppCompatActivity() {

    private var viewModel: CovidViewModel? = null
    private var binding: ActivityMainBinding? = null
    private val compositeDisposable = CompositeDisposable()
    private var currentChartedData: List<CovidData>? = null

    private val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CovidViewModel(
                    covidDataApi = getCovidApp().retrofit.create(CovidDataApi::class.java),
                    covidDataDao = getCovidApp().databaseHelper.covidDb.covidDataDao(),
                    lastUpdatedData = getCovidApp().lastUpdatedData,
                    stringGetter = getCovidApp().stringGetter
            ) as T
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CovidViewModel::class.java)
        binding?.viewModel = viewModel

        viewModel?.stateSubject?.subscribe {
            plotData(it.chartedData)
        }?.addToComposite(compositeDisposable)


    }

    private fun plotData(covidDatum: List<CovidData>?) {

        if (covidDatum != currentChartedData) { //todo test if this works
            currentChartedData = covidDatum

            val entries = arrayListOf<Entry>()
            covidDatum?.forEach {

                val dateFloat = it.date?.time?.toFloat()
                val threeDayAvg = it.threeDayPostiveTestRateAvg?.toFloat()

                if (dateFloat != null && threeDayAvg != null) {
                    entries.add(Entry(dateFloat, threeDayAvg))
                }
            }

            val dataPlot = LineDataSet(entries, "Three Day Average")
            dataPlot.axisDependency = YAxis.AxisDependency.LEFT

            binding?.coronaGraph?.data = LineData(arrayListOf(dataPlot) as List<ILineDataSet>?)
            binding?.coronaGraph?.invalidate()

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun getCovidApp(): CovidApp {
        return applicationContext as CovidApp
    }
}
