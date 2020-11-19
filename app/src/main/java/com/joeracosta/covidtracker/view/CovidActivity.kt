package com.joeracosta.covidtracker.view

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.snackbar.Snackbar
import com.joeracosta.covidtracker.CovidApp
import com.joeracosta.covidtracker.R
import com.joeracosta.covidtracker.TimeUtil
import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.CovidData
import com.joeracosta.covidtracker.data.CovidDataApi
import com.joeracosta.covidtracker.data.DataToPlot
import com.joeracosta.covidtracker.data.State
import com.joeracosta.covidtracker.databinding.ActivityMainBinding
import com.joeracosta.covidtracker.viewmodel.CovidViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class CovidActivity : AppCompatActivity() {

    private var viewModel: CovidViewModel? = null
    private var binding: ActivityMainBinding? = null
    private val compositeDisposable = CompositeDisposable()
    private var currentChartedData: List<CovidData>? = null
    private var currentPlottedDataType: DataToPlot? = null

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
        binding?.coronaGraph?.setTouchEnabled(false)
        binding?.coronaGraph?.xAxis?.position = XAxis.XAxisPosition.BOTTOM
        binding?.coronaGraph?.description = null

        configureSpinner()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CovidViewModel::class.java)
        binding?.viewModel = viewModel

        viewModel?.errorDisplay
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe {
                showError()
            }?.addToComposite(compositeDisposable)

        viewModel?.stateSubject
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe {

                val currentSelectedUSState = binding?.stateSpinner?.selectedItem

                if (currentSelectedUSState != it.selectedUsaState) {
                    val position = State.values().indexOf(it.selectedUsaState)
                    binding?.stateSpinner?.setSelection(position)
                }

                setDataPlotSelector(it.dataToPlot)
                setDateSelectorBasedOnTime(it.amountOfDaysAgoToShow)
                plotData(it.chartedData, it.dataToPlot)
            }?.addToComposite(compositeDisposable)


    }

    private fun setDataPlotSelector(dataToPlot: DataToPlot?) {
        val indexToSelect = when (dataToPlot) {
            DataToPlot.POSITIVE_CASE_RATE -> 0
            DataToPlot.NEW_HOSPITALIZATIONS -> 1
            else -> -1
        }

        var currentlySelectedIndex: Int? = null
        val currentlySelectedRadioButtonId = binding?.dataToPlotPicker?.checkedRadioButtonId
        if (currentlySelectedRadioButtonId != null) {
            val currentlySelectedRadioButton =
                binding?.dataToPlotPicker?.findViewById<RadioButton>(currentlySelectedRadioButtonId)
            if (currentlySelectedRadioButton != null) {
                currentlySelectedIndex =
                    binding?.dataToPlotPicker?.indexOfChild(currentlySelectedRadioButton)
            }
        }

        if (indexToSelect != -1 && currentlySelectedIndex != indexToSelect) {
            (binding?.dataToPlotPicker?.getChildAt(indexToSelect) as RadioButton).isChecked = true
        }


    }

    private fun setDateSelectorBasedOnTime(amountOfDaysAgoToShow: Int?) {

        if (amountOfDaysAgoToShow == null) return

        val indexToSelect = when (amountOfDaysAgoToShow) {
            TimeUtil.ALL_TIME_DAYS -> 0
            TimeUtil.SIX_MONTHS_DAYS -> 1
            TimeUtil.THREE_MONTHS_DAYS -> 2
            TimeUtil.ONE_MONTH_DAYS -> 3
            TimeUtil.TWO_WEEKS_DAYS -> 4
            TimeUtil.FIVE_DAYS -> 5
            else -> -1
        }

        var currentlySelectedIndex: Int? = null

        val currentlySelectedRadioButtonId = binding?.timeFrame?.checkedRadioButtonId
        if (currentlySelectedRadioButtonId != null) {
            val currentlySelectedRadioButton =
                binding?.timeFrame?.findViewById<RadioButton>(currentlySelectedRadioButtonId)
            if (currentlySelectedRadioButton != null) {
                currentlySelectedIndex =
                    binding?.timeFrame?.indexOfChild(currentlySelectedRadioButton)
            }
        }

        if (indexToSelect != -1 && currentlySelectedIndex != indexToSelect) {
            (binding?.timeFrame?.getChildAt(indexToSelect) as RadioButton).isChecked = true
        }

    }

    private fun configureSpinner() {

        binding?.stateSpinner?.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, State.values())

        binding?.stateSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    State.values().getOrNull(position)?.let {
                        viewModel?.setSelectedUSState(it)
                    }
                }

            }
    }

    private fun plotData(covidDatum: List<CovidData>?, dataToPlot: DataToPlot?) {

        if (covidDatum != currentChartedData || currentPlottedDataType != dataToPlot) {
            currentChartedData = covidDatum
            currentPlottedDataType = dataToPlot

            val entries = arrayListOf<Entry>()
            covidDatum?.forEach {

                val dateFloat = it.date?.time?.toFloat()

                val dataToDisplay = when (dataToPlot) {
                    DataToPlot.POSITIVE_CASE_RATE -> it.postiveTestRateSevenDayAvg?.toFloat()
                    DataToPlot.NEW_HOSPITALIZATIONS -> it.newHospitalizationsThreeDayAverage?.toFloat()
                    else -> null
                }

                if (dateFloat != null && dataToDisplay != null) {
                    entries.add(Entry(dateFloat, dataToDisplay))
                }
            }

            val dataSet =
                LineDataSet(entries, getCovidApp().stringGetter.getString(R.string.seven_day_avg))
            dataSet.axisDependency = YAxis.AxisDependency.LEFT

            dataSet.lineWidth = 3f
            dataSet.setCircleColor(ContextCompat.getColor(this, R.color.colorAccent))
            dataSet.circleHoleColor = ContextCompat.getColor(this, R.color.colorAccent)
            dataSet.color = ContextCompat.getColor(this, R.color.colorAccent)
            dataSet.setDrawValues(false)

            binding?.coronaGraph?.data =
                LineData(arrayListOf(dataSet) as List<ILineDataSet>?)
            binding?.coronaGraph?.invalidate()

        }
    }

    private fun showError() {
        val rootView = binding?.root ?: return
        Snackbar.make(
            rootView,
            getCovidApp().stringGetter.getString(R.string.error_fetching_data),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun getCovidApp(): CovidApp {
        return applicationContext as CovidApp
    }
}
