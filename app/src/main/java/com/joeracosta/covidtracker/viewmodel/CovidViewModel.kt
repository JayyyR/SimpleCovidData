package com.joeracosta.covidtracker.viewmodel

import androidx.databinding.Bindable
import androidx.lifecycle.ViewModel
import com.joeracosta.covidtracker.BaseObservableViewModel
import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.CovidDataApi
import com.joeracosta.covidtracker.data.CovidDataRepo
import com.joeracosta.covidtracker.data.CovidState
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class CovidViewModel(
    private val covidDataApi: CovidDataApi,
    private val covidDataDao: CovidDataDao
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

    fun refreshData() {
        updateState(
            currentState.copy(updatingData = true)
        )

        updateDisposable = covidDataRepo.fetchLatestCovidData()
            .subscribe {
                updateState(
                    currentState.copy(updatingData = false)
                )
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