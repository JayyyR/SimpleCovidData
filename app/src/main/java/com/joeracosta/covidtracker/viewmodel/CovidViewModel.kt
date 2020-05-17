package com.joeracosta.covidtracker.viewmodel

import android.annotation.SuppressLint
import androidx.databinding.Bindable
import com.joeracosta.covidtracker.BaseObservableViewModel
import com.joeracosta.covidtracker.R
import com.joeracosta.covidtracker.data.*
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.text.SimpleDateFormat
import java.util.*

class CovidViewModel(
    private val covidDataApi: CovidDataApi,
    private val covidDataDao: CovidDataDao,
    private val lastUpdatedData: LastUpdatedData,
    private val stringGetter: StringGetter
) : BaseObservableViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private var updateDisposable: Disposable? = null
    val stateSubject = BehaviorSubject.createDefault(CovidState(
        selectedUsaState = lastUpdatedData.getSelectedUSState(),
        showDataFromDate = Date().apply {
            time = System.currentTimeMillis() - (90 * DAY)
        } //todo no default
    )
    )

    private var daoDisposable: Disposable? = null
    private val currentState: CovidState
        get() = stateSubject.value ?: CovidState(
            selectedUsaState = lastUpdatedData.getSelectedUSState(),
            showDataFromDate = Date().apply {
                time = System.currentTimeMillis() - (90 * DAY)
            } //todo no default
        )

    private val covidDataRepo = CovidDataRepo(
        covidDataApi = covidDataApi,
        compositeDisposable = compositeDisposable,
        covidDataDao = covidDataDao
    )

    init {
        checkDBForChartData()
        val updatedMoreThanADayAgo =
            lastUpdatedData.getLastUpdatedTime() + DAY < System.currentTimeMillis()

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
                }

                updateDisposable?.dispose()
            }
    }

    private fun checkDBForChartData() {
        val selectedUsaState = currentState.selectedUsaState
        val selectedAfterDate = currentState.showDataFromDate

        //only open a new connection if the params have changed
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
                    //todo error
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

    private fun updateState(newCovidState: CovidState) {

        val stateSelectedBefore = currentState.selectedUsaState
        val dateAfterSelectedBefore = currentState.showDataFromDate
        stateSubject.onNext(
            newCovidState
        )

        notifyChange()

        //only check if data is new
        if (stateSelectedBefore != currentState.selectedUsaState || dateAfterSelectedBefore != currentState.showDataFromDate) {
            checkDBForChartData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        daoDisposable?.dispose()
    }

    companion object {
        const val DAY = 24 * 60 * 60 * 1000.toLong()
    }

}