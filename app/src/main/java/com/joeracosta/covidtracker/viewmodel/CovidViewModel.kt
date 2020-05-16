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
    private val stateSubject = BehaviorSubject.createDefault(CovidState())

    private val currentState: CovidState get() = stateSubject.value ?: CovidState()

    private val covidDataRepo = CovidDataRepo(
        covidDataApi = covidDataApi,
        compositeDisposable = compositeDisposable,
        covidDataDao = covidDataDao
    )

    init {
        //todo check if I need to refresh data based on time
        refreshData()
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
        val formatted =  format.format(timeLastUpdated.time)

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

    private fun updateState(newCovidState: CovidState) {
        stateSubject.onNext(
            newCovidState
        )

        notifyChange()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}