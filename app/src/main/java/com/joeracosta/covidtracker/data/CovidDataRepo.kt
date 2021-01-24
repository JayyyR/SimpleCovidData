package com.joeracosta.covidtracker.data

import android.annotation.SuppressLint
import com.joeracosta.covidtracker.addToComposite
import com.joeracosta.covidtracker.data.db.CovidDataDao
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.ResponseBody
import java.text.SimpleDateFormat
import java.util.*

class CovidDataRepo(
    private val covidTrackingProjectApi: CovidTrackingProjectApi,
    private val ourWorldInDataApi: OurWorldInDataApi,
    private val compositeDisposable: CompositeDisposable,
    private val covidDataDao: CovidDataDao
) {


    /**
     * Fetches data from server, returns a stream that indicates whether we succeeded gathering and
     * storing latest data
     */
    fun fetchLatestCovidData(): Observable<Boolean> {
        val successObservable = BehaviorSubject.create<Boolean>()

        Flowable.zip(
            covidTrackingProjectApi.getStateData(),
            covidTrackingProjectApi.getUSData(),
            ourWorldInDataApi.vaccinationUSData(),
            ourWorldInDataApi.vaccinationStateData(),
            Function4 { stateData: List<CovidRawData>,
                        usData: List<CovidRawData>,
                        usVaccinationResponse: ResponseBody,
                        stateVaccinationResponse: ResponseBody ->

                val usDataWithVaccinations = addVaccinations(
                    rawVaccineResponse = usVaccinationResponse,
                    covidData = usData.map {
                        it.copy(
                            location = Location.UNITED_STATES
                        ).toCovidData()
                    },
                    isCountryData = true
                )

                val stateDataWithVaccinations = addVaccinations(
                    rawVaccineResponse = stateVaccinationResponse,
                    covidData = stateData.map { it.toCovidData() },
                    isCountryData = false
                )

                //combine state data with US Data
                listOf(
                    stateDataWithVaccinations,
                    usDataWithVaccinations
                ).flatten()

            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ covidData: List<CovidData> ->

                val dataWithSevenDayAverages = calculateSevenDayAverages(covidData)

                updateDatabaseData(dataWithSevenDayAverages).subscribe({ success ->
                    successObservable.onNext(success)
                }, {
                    successObservable.onNext(false)
                })

            }, {
                successObservable.onNext(false)
            }).addToComposite(compositeDisposable)

        return successObservable
    }

    @SuppressLint("SimpleDateFormat")
    private fun addVaccinations(
        rawVaccineResponse: ResponseBody,
        covidData: List<CovidData>,
        isCountryData: Boolean
    ): List<CovidData> {
        val bufferedSource = rawVaccineResponse.source()

        val header = bufferedSource.readUtf8Line()
        val headerTitles = header?.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))

        val dateIndex = headerTitles?.indexOf(VACCINE_DATA_DATE_HEADER_TITLE) ?: return covidData
        val totalPeopleVaccinatedIndex =
            headerTitles.indexOf(VACCINE_DATA_TOTAL_PEOPLE_VACCINATED_HEADER_TITLE)
        val percentVaccinatedIndex = headerTitles.indexOf(VACCINE_DATA_PEOPLE_VACCINATED_PERCENT)
        val locationIndex = headerTitles.indexOf(VACCINE_DATA_LOCATION_HEADER_TITLE)

        val temporaryVaccinationData = mutableMapOf<Location, MutableList<CovidData>>()

        var csvLine = bufferedSource.readUtf8Line()
        while (csvLine != null) {

            val valuesArray = csvLine.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
            val dateString = valuesArray.getOrNull(dateIndex)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date = dateString?.let {
                dateFormat.parse(dateString)
            }
            val locationString = if (isCountryData) Location.UNITED_STATES.toString() else valuesArray.getOrNull(locationIndex)
            val peopleVaccinated = valuesArray.getOrNull(totalPeopleVaccinatedIndex)?.toDoubleOrNull()
            val percentVaccinated = if (isCountryData) {
                if (peopleVaccinated == null) {
                    null
                } else {
                    ((peopleVaccinated) / US_POPULATION) * 100
                }
            } else {
                valuesArray.getOrNull(percentVaccinatedIndex)?.toDoubleOrNull()
            }

            val location = Location.getLocationFromString(locationString)

            val previousDayWithData = if (location != null) temporaryVaccinationData[location]?.findLast { it.totalPeopleVaccinated != null } else null

            //estimate new vaccinations by dividing by days between vaccine data since there are gaps in the data
            val daysBetweenVaccineData = if (previousDayWithData != null) daysBetween(previousDayWithData.date, date) else null
            val newVaccinations = peopleVaccinated?.let {
               (peopleVaccinated - (previousDayWithData?.totalPeopleVaccinated?.toDouble() ?: 0.0)) / (daysBetweenVaccineData ?: 1)
            }

            if (date != null && location != null) {
                if (temporaryVaccinationData[location] == null) {
                    temporaryVaccinationData[location] = mutableListOf()
                }

                temporaryVaccinationData[location]?.add(
                    CovidData(
                        date = date,
                        location = location,
                        totalPeopleVaccinated = peopleVaccinated?.toLong(),
                        newPeopleVaccinated = newVaccinations?.toLong(),
                        percentOfPopulationVaccinated = percentVaccinated
                    )
                )
            }

            csvLine = bufferedSource.readUtf8Line()
        }

        //combine data
        return covidData.map { dataWithoutVaccinations ->

            val matchingVaccinationData =
                temporaryVaccinationData[dataWithoutVaccinations.location]?.find { it.date == dataWithoutVaccinations.date }
            if (matchingVaccinationData != null) {
                return@map dataWithoutVaccinations.copy(
                    totalPeopleVaccinated = matchingVaccinationData.totalPeopleVaccinated,
                    newPeopleVaccinated = matchingVaccinationData.newPeopleVaccinated,
                    percentOfPopulationVaccinated = matchingVaccinationData.percentOfPopulationVaccinated
                )
            }

            return@map dataWithoutVaccinations
        }
    }

    private fun calculateSevenDayAverages(covidData: List<CovidData>): List<CovidData> {

        val mapOfLocationData = hashMapOf<Location, ArrayList<CovidData>>()

        covidData.forEach {

            val loc = it.location ?: return@forEach

            //add empty list of needed
            if (!mapOfLocationData.containsKey(loc)) {
                mapOfLocationData[loc] = arrayListOf()
            }

            mapOfLocationData[loc]?.add(it)
        }

        val listOfLocationDataWithAverages = mapOfLocationData.values.map { list ->
            //sort by date
            list.sortBy {
                it.date
            }

            //calculate averages
            val listDataWithSevenDayAvgs = list.mapIndexed { index, covidData ->

                val lastSevenDaysPositiveRate = if (index >= 6) {
                    list.slice(index - 6..index).map {
                        it.postiveTestRate
                    }
                } else {
                    null
                }

                val dayOfFirstVaccinations = Calendar.getInstance()
                    .apply {
                        set(2020, 11, 14, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                val daysBetweenCurrentAndFirstDay =
                    daysBetween(dayOfFirstVaccinations.time, covidData.date)
                val daysToCalculateVaccineAvgsWith = minOf(
                    daysBetweenCurrentAndFirstDay,
                    6
                ) //calculate last 7 days or only days since vaccination startedd

                val lastSevenOrLessDaysNewVaccinations =
                    if (daysBetweenCurrentAndFirstDay >= 0 && index >= daysToCalculateVaccineAvgsWith) {
                        list.slice(index - daysToCalculateVaccineAvgsWith..index).map {

                            val newVaccinationsToReturn = it.newPeopleVaccinated

                            newVaccinationsToReturn
                        }
                    } else {
                        null
                    }

                //sometimes days have null data because of garabage from the API but we still want to calculate averages from the data we have
                val positiveRateFromActualDaysWithDataFromLastSeven =
                    lastSevenDaysPositiveRate?.filterNotNull()

                //this is also necessary because some dates don't have data
                val newVaccinationsFromActualDaysWithDataFromLastSeven =
                    lastSevenOrLessDaysNewVaccinations?.filterNotNull()

                val postiveTestRateSevenDayAvg =
                    if (positiveRateFromActualDaysWithDataFromLastSeven != null) {
                        (positiveRateFromActualDaysWithDataFromLastSeven.sumByDouble { it }) / positiveRateFromActualDaysWithDataFromLastSeven.size //we only calculate avg with days that had data
                    } else {
                        null
                    }

                val newVaccinationsSevenDayAvg =
                    if (newVaccinationsFromActualDaysWithDataFromLastSeven?.isNotEmpty() == true) {
                        newVaccinationsFromActualDaysWithDataFromLastSeven.sumByDouble { it.toDouble() } / newVaccinationsFromActualDaysWithDataFromLastSeven.size //calculate avg with data we have (not all days have data)
                    } else {
                        null
                    }

                covidData.copy(
                    postiveTestRateSevenDayAvg = postiveTestRateSevenDayAvg,
                    newVaccinationsSevenDayAvg = newVaccinationsSevenDayAvg
                )

            }

            listDataWithSevenDayAvgs
        }

        return listOfLocationDataWithAverages.flatten()

    }

    private fun updateDatabaseData(data: List<CovidData>): Observable<Boolean> {
        val successObservable = BehaviorSubject.create<Boolean>()

        Single.fromCallable {
            covidDataDao.clearAllData()
        }
            .subscribeOn(Schedulers.io())
            .subscribe({
                Single.fromCallable {
                    covidDataDao.insertData(data)
                }
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        successObservable.onNext(true)
                    }, {
                        successObservable.onNext(false)
                    })

            }, {
                successObservable.onNext(false)
            }).addToComposite(compositeDisposable)

        return successObservable
    }

    private fun daysBetween(d1: Date?, d2: Date?): Int {
        if (d1 == null || d2 == null) return -1
        val difference = ((d2.time - d1.time) / (1000 * 60 * 60 * 24)).toInt()
        return difference
    }

    companion object {
        const val VACCINE_DATA_DATE_HEADER_TITLE = "date"
        const val VACCINE_DATA_TOTAL_PEOPLE_VACCINATED_HEADER_TITLE = "people_vaccinated"
        const val VACCINE_DATA_LOCATION_HEADER_TITLE = "location"
        const val VACCINE_DATA_PEOPLE_VACCINATED_PERCENT = "people_vaccinated_per_hundred"

        const val US_POPULATION = 328200000.0
    }

}