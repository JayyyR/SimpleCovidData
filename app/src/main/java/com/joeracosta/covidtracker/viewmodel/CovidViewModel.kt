package com.joeracosta.covidtracker.viewmodel

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Resources
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.Bindable
import com.github.mikephil.charting.data.Entry
import com.joeracosta.covidtracker.BaseObservableViewModel
import com.joeracosta.covidtracker.R
import com.joeracosta.covidtracker.TimeUtil
import com.joeracosta.covidtracker.TimeUtil.ALL_TIME_DAYS
import com.joeracosta.covidtracker.TimeUtil.DAY_MILLIS
import com.joeracosta.covidtracker.data.*
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

class CovidViewModel(
    private val covidTrackingProjectApi: CovidTrackingProjectApi,
    private val ourWorldInDataApi: OurWorldInDataApi,
    private val covidDataDao: CovidDataDao,
    private val lastUpdatedData: LastUpdatedData,
    private val stringGetter: StringGetter,
    private val appResources: Resources
) : BaseObservableViewModel() {

    private val defaultState = CovidState(
        selectedUsaLocation = lastUpdatedData.getSelectedUSState() ?: Location.UNITED_STATES,
        amountOfDaysAgoToShow = lastUpdatedData.getAmountOfDaysAgoToShow() ?: ALL_TIME_DAYS,
        dataToPlot = lastUpdatedData.getDataToPlot() ?: DataToPlot.NEW_CASES
    )

    private val compositeDisposable = CompositeDisposable()
    private var updateDisposable: Disposable? = null
    val stateSubject = BehaviorSubject.createDefault(defaultState)

    val errorDisplay = PublishSubject.create<Boolean>()

    private var daoDisposable: Disposable? = null
    private val currentState: CovidState
        get() = stateSubject.value ?: defaultState

    private val covidDataRepo = CovidDataRepo(
        covidTrackingProjectApi = covidTrackingProjectApi,
        ourWorldInDataApi = ourWorldInDataApi,
        compositeDisposable = compositeDisposable,
        covidDataDao = covidDataDao
    )

    init {
        openConnectionToDBData()
        val updatedMoreThanADayAgo =
            lastUpdatedData.getLastUpdatedTime() + DAY_MILLIS < System.currentTimeMillis()

        if (updatedMoreThanADayAgo) {
            refreshData()
        }
    }

    val covidDataPlotIndexListener: (Int) -> Unit = { index ->
        when (index) {
            0 -> setDataToPlot(DataToPlot.NEW_CASES)
            1 -> setDataToPlot(DataToPlot.NEW_DEATHS)
        }
    }

    val vaccineDataPlotIndexListener: (Int) -> Unit = { index ->
        when (index) {
            0 -> setDataToPlot(DataToPlot.NEW_VACCINATIONS)
            1 -> setDataToPlot(DataToPlot.TOTAL_VACCINATIONS)
            2 -> setDataToPlot(DataToPlot.PERCENT_VACCINATED)
        }
    }

    val timeFrameIndexListener: (Int) -> Unit = { index ->
        when (index) {
            0 -> setSelectedTimeFrame(TimeUtil.ALL_TIME_DAYS)
            1 -> setSelectedTimeFrame(TimeUtil.SIX_MONTHS_DAYS)
            2 -> setSelectedTimeFrame(TimeUtil.THREE_MONTHS_DAYS)
            3 -> setSelectedTimeFrame(TimeUtil.ONE_MONTH_DAYS)
            4 -> setSelectedTimeFrame(TimeUtil.TWO_WEEKS_DAYS)
            5 -> setSelectedTimeFrame(TimeUtil.FIVE_DAYS)
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun getXAxisFormatter(): (Float) -> String {
        val labelCalender = Calendar.getInstance()
        val format = SimpleDateFormat("MM/dd")

        return { value ->
            labelCalender.timeInMillis = value.toLong()
            val date = format.format(labelCalender.time)
            date
        }
    }

    @Bindable
    fun getUpdatingData(): Boolean {
        return currentState.updatingData == true
    }

    @Bindable
    fun getChartTitle(): String {
        return when (currentState.dataToPlot) {
            DataToPlot.NEW_CASES -> stringGetter.getString(R.string.new_cases_chart_title)
            DataToPlot.NEW_DEATHS -> stringGetter.getString(R.string.new_deaths_chart_title)
            DataToPlot.NEW_VACCINATIONS -> stringGetter.getString(R.string.new_vaccinations_chart_title)
            DataToPlot.TOTAL_VACCINATIONS -> stringGetter.getString(R.string.total_vaccinations_chart_title)
            DataToPlot.PERCENT_VACCINATED -> stringGetter.getString(R.string.percent_vaccinated_chart_title)
            else -> ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Bindable
    fun getLastUpdatedText(): String {

        if (getUpdatingData()) return stringGetter.getString(R.string.updating)

        val lastUpdatedTime = lastUpdatedData.getLastUpdatedTime()

        val timeLastUpdated = Calendar.getInstance().apply {
            timeInMillis = lastUpdatedTime
        }

        val format = SimpleDateFormat("MM/dd hh:mm aa")
        val formatted = format.format(timeLastUpdated.time)

        return if (lastUpdatedTime == 0L) stringGetter.getString(R.string.never_updated) else "Last Updated $formatted"
    }

    @Bindable
    fun getDisclaimerText(): String {
        return when (currentState.dataToPlot) {
            else -> ""
        }
    }

    @Bindable
    fun getSubtitleText(): String {
        return when (currentState.dataToPlot) {
            DataToPlot.NEW_CASES, DataToPlot.NEW_VACCINATIONS, DataToPlot.NEW_DEATHS -> stringGetter.getString(R.string.chart_subtitle_seven_day_avg)
            else -> ""
        }
    }

    @Bindable
    fun getBackgroundForDateRadioButton(): Int {
        return when (currentState.dataToPlot) {
            DataToPlot.NEW_CASES, DataToPlot.NEW_DEATHS -> R.drawable.radio_flat_selector_red
            DataToPlot.NEW_VACCINATIONS, DataToPlot.TOTAL_VACCINATIONS, DataToPlot.PERCENT_VACCINATED -> R.drawable.radio_flat_selector_blue
            else -> R.drawable.radio_flat_selector_red
        }
    }

    fun getChartEntriesFromDatum(covidDatum: List<CovidData>?, dataToPlot: DataToPlot?): List<Entry> {

        val entries = arrayListOf<Entry>()
        val latestDayWithVaccinationTotals = covidDatum?.findLast { it.totalPeopleVaccinated != null }
        val latestDayWithVaccinationTotalsIndexRaw = covidDatum?.indexOf(latestDayWithVaccinationTotals)

        val latestDayWithVaccinationTotalsIndex = if (latestDayWithVaccinationTotalsIndexRaw == -1 || latestDayWithVaccinationTotalsIndexRaw == null) Int.MAX_VALUE else latestDayWithVaccinationTotalsIndexRaw

        covidDatum?.forEachIndexed { index, it ->
            val dateFloat = it.date?.time?.toFloat()

            val dataToDisplay = when (dataToPlot) {
                DataToPlot.NEW_CASES -> it.newCasesSevenDayAvg?.toFloat()
                DataToPlot.NEW_DEATHS -> it.newDeathsSevenDayAvg?.toFloat()
                DataToPlot.NEW_VACCINATIONS -> {
                    //don't plot days we don't have data for yet
                    if (index > latestDayWithVaccinationTotalsIndex) {
                        null
                    } else {
                        it.newVaccinationsSevenDayAvg?.toFloat()
                    }
                }
                DataToPlot.TOTAL_VACCINATIONS -> it.totalPeopleVaccinated?.toFloat()
                DataToPlot.PERCENT_VACCINATED -> it.percentOfPopulationVaccinated?.toFloat()
                else -> null
            }

            if (dateFloat != null && dataToDisplay != null) {
                entries.add(Entry(dateFloat, dataToDisplay))
            }
        }

        return entries
    }

    @Bindable
    fun getChartVisibility(): Int {
        val entriesToShow = getChartEntriesFromDatum(currentState.chartedData, currentState.dataToPlot)
        return if (entriesToShow.isEmpty()) View.INVISIBLE else View.VISIBLE
    }

    @Bindable
    fun getNoDataVisibility(): Int {
        val entriesToShow = getChartEntriesFromDatum(currentState.chartedData, currentState.dataToPlot)
        return if (entriesToShow.isEmpty()) View.VISIBLE else View.GONE
    }

    @Bindable
    fun getTextColorForDateRadioButton(): ColorStateList? {
        val resourceId = when (currentState.dataToPlot) {
            DataToPlot.NEW_CASES, DataToPlot.NEW_DEATHS -> R.drawable.radio_flat_text_selector_red
            DataToPlot.NEW_VACCINATIONS, DataToPlot.TOTAL_VACCINATIONS, DataToPlot.PERCENT_VACCINATED -> R.drawable.radio_flat_text_selector_blue
            else -> R.drawable.radio_flat_text_selector_red
        }

        return ResourcesCompat.getColorStateList(appResources, resourceId, null)
    }

    fun refreshData() {
        updateState(
            currentState.copy(updatingData = true)
        )

        updateDisposable = covidDataRepo.fetchLatestCovidData()
            .subscribe { success ->
                updateState(
                    currentState.copy(updatingData = false)
                )

                if (success) {
                    lastUpdatedData.setLastUpdatedTime(System.currentTimeMillis())
                } else {
                    errorDisplay.onNext(true)
                }

                updateDisposable?.dispose()
            }
    }

    private fun openConnectionToDBData() {
        val selectedUsaState = currentState.selectedUsaLocation
        val amountOfDaysAgoToShow = currentState.amountOfDaysAgoToShow

        val selectedAfterDate = getDateToShowFrom(amountOfDaysAgoToShow)

        if (selectedUsaState != null && selectedAfterDate != null) {
            daoDisposable?.dispose()
            daoDisposable = covidDataDao.getCovidDataAfterDate(
                selectedUsaState.postalCode,
                selectedAfterDate
            )
                .subscribe({
                    currentState.selectedUsaLocation?.let(lastUpdatedData::setSelectedUSState)
                    updateState(
                        currentState.copy(
                            chartedData = it
                        )
                    )
                }, {
                    errorDisplay.onNext(true)
                })
        }
    }

    fun setSelectedUSState(selectedUSALocation: Location) {
        if (selectedUSALocation == currentState.selectedUsaLocation) return

        updateState(
            currentState.copy(
                selectedUsaLocation = selectedUSALocation
            )
        )
    }

    fun setDataToPlot(dataToPlot: DataToPlot) {
        lastUpdatedData.setDataToPlot(dataToPlot)
        updateState(
            currentState.copy(
                dataToPlot = dataToPlot
            )
        )
    }

    fun setSelectedTimeFrame(amountOfDaysAgoToShow: Int) {
        lastUpdatedData.setAmountOfDaysAgoToShow(amountOfDaysAgoToShow)
        updateState(
            currentState.copy(
                amountOfDaysAgoToShow = amountOfDaysAgoToShow
            )
        )
    }

    private fun updateState(newCovidState: CovidState) {

        val stateSelectedBefore = currentState.selectedUsaLocation
        val amountOfDaysAgoToShow = currentState.amountOfDaysAgoToShow
        stateSubject.onNext(
            newCovidState
        )

        notifyChange()

        //only check if data is new
        if (stateSelectedBefore != currentState.selectedUsaLocation || amountOfDaysAgoToShow != currentState.amountOfDaysAgoToShow) {
            openConnectionToDBData()
        }
    }


    private fun getDateToShowFrom(amountOfDaysAgoToShow: Int?): Date? {
        if (amountOfDaysAgoToShow == null) return null

        return Date().apply {
            time = System.currentTimeMillis() - (amountOfDaysAgoToShow * DAY_MILLIS)
        }
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        daoDisposable?.dispose()
    }

}