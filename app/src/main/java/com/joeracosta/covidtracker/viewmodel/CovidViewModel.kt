package com.joeracosta.covidtracker.viewmodel

import android.annotation.SuppressLint
import androidx.databinding.Bindable
import com.joeracosta.covidtracker.BaseObservableViewModel
import com.joeracosta.covidtracker.R
import com.joeracosta.covidtracker.TimeUtil.DAY_MILLIS
import com.joeracosta.covidtracker.TimeUtil.TWO_MONTHS_DAYS
import com.joeracosta.covidtracker.data.*
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

class CovidViewModel(
    private val covidDataApi: CovidDataApi,
    private val covidDataDao: CovidDataDao,
    private val lastUpdatedData: LastUpdatedData,
    private val stringGetter: StringGetter
) : BaseObservableViewModel() {

    private val defaultState = CovidState(
        selectedUsaState = lastUpdatedData.getSelectedUSState() ?: State.NEW_YORK,
        amountOfDaysAgoToShow = lastUpdatedData.getAmountOfDaysAgoToShow() ?: TWO_MONTHS_DAYS
    )

    private val compositeDisposable = CompositeDisposable()
    private var updateDisposable: Disposable? = null
    val stateSubject = BehaviorSubject.createDefault(defaultState)

    val errorDisplay = PublishSubject.create<Boolean>()

    private var daoDisposable: Disposable? = null
    private val currentState: CovidState
        get() = stateSubject.value ?: defaultState

    private val covidDataRepo = CovidDataRepo(
        covidDataApi = covidDataApi,
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
        val selectedUsaState = currentState.selectedUsaState
        val amountOfDaysAgoToShow = currentState.amountOfDaysAgoToShow

        val selectedAfterDate = getDateToShowFrom(amountOfDaysAgoToShow)

        if (selectedUsaState != null && selectedAfterDate != null) {
            daoDisposable?.dispose()
            daoDisposable = covidDataDao.getPostiveRateByStateAfterDate(
                selectedUsaState.postalCode,
                selectedAfterDate
            )
                .subscribe({
                    currentState.selectedUsaState?.let(lastUpdatedData::setSelectedUSState)
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

    fun setSelectedUSState(selectedUSAState: State) {
        if (selectedUSAState == currentState.selectedUsaState) return

        updateState(
            currentState.copy(
                selectedUsaState = selectedUSAState
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

        val stateSelectedBefore = currentState.selectedUsaState
        val amountOfDaysAgoToShow = currentState.amountOfDaysAgoToShow
        stateSubject.onNext(
            newCovidState
        )

        notifyChange()

        //only check if data is new
        if (stateSelectedBefore != currentState.selectedUsaState || amountOfDaysAgoToShow != currentState.amountOfDaysAgoToShow) {
            openConnectionToDBData()
        }
    }


    private fun getDateToShowFrom(amountOfDaysAgoToShow: Int?): Date? {
        if (amountOfDaysAgoToShow == null ) return null

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