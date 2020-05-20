package com.joeracosta.covidtracker.view

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.snackbar.Snackbar
import com.joeracosta.covidtracker.CovidApp
import com.joeracosta.covidtracker.R
import com.joeracosta.covidtracker.TimeUtil
import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.CovidData
import com.joeracosta.covidtracker.data.CovidDataApi
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
        configureDateSelector()

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

                setDateSelectorBasedOnTime(it.amountOfDaysAgoToShow)
                plotData(it.chartedData)
            }?.addToComposite(compositeDisposable)


    }

    private fun setDateSelectorBasedOnTime(amountOfDaysAgoToShow: Int?) {

        if (amountOfDaysAgoToShow == null) return

        val indexToSelect = when (amountOfDaysAgoToShow) {
            TimeUtil.THREE_MONTHS_DAYS -> 0
            TimeUtil.TWO_MONTHS_DAYS -> 1
            TimeUtil.ONE_MONTH_DAYS -> 2
            TimeUtil.TWO_WEEKS_DAYS -> 3
            TimeUtil.FIVE_DAYS -> 4
            else -> -1
        }

        val currentlySelectedRadioButtonId = binding?.timeFrame?.checkedRadioButtonId
        var currentlySelectedIndex: Int? = null

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

    private fun configureDateSelector() {

        binding?.timeFrame?.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                val radioButton = binding?.timeFrame?.findViewById<RadioButton>(checkedId)
                val index = binding?.timeFrame?.indexOfChild(radioButton)

                when (index) {
                    0 -> {
                        viewModel?.setSelectedTimeFrame(TimeUtil.THREE_MONTHS_DAYS)
                    }
                    1 -> {
                        viewModel?.setSelectedTimeFrame(TimeUtil.TWO_MONTHS_DAYS)
                    }
                    2 -> {
                        viewModel?.setSelectedTimeFrame(TimeUtil.ONE_MONTH_DAYS)
                    }
                    3 -> {
                        viewModel?.setSelectedTimeFrame(TimeUtil.TWO_WEEKS_DAYS)
                    }
                    4 -> {
                        viewModel?.setSelectedTimeFrame(TimeUtil.FIVE_DAYS)
                    }
                }
            }

        })

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

    private fun plotData(covidDatum: List<CovidData>?) {

        if (covidDatum != currentChartedData) {
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

            dataPlot.lineWidth = 3f
            dataPlot.setCircleColor(ContextCompat.getColor(this, R.color.colorAccent))
            dataPlot.circleHoleColor = ContextCompat.getColor(this, R.color.colorAccent)
            dataPlot.color = ContextCompat.getColor(this, R.color.colorAccent)
            dataPlot.setDrawValues(false)


            binding?.coronaGraph?.data = LineData(arrayListOf(dataPlot) as List<ILineDataSet>?)
            binding?.coronaGraph?.invalidate()

        }

    }

    private fun showError() {
        val rootView = binding?.root ?: return
        Snackbar.make(rootView, getCovidApp().stringGetter.getString(R.string.error_fetching_data), Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun getCovidApp(): CovidApp {
        return applicationContext as CovidApp
    }
}
