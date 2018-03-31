package com.brainasaservice.rxblegatt.advertiser

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSet
import io.reactivex.Completable
import io.reactivex.Observable

interface RxBleAdvertiser {

    var data: AdvertiseData?

    var settings: AdvertiseSettings?

    var response: AdvertiseData?

    fun start(): Completable

    fun stop(): Completable

    fun status(): Observable<RxBleAdvertiser.Status>


    sealed class Status {
        object Active : Status()
        object Inactive : Status()
        data class Error(val error: Throwable) : Status()
    }

    sealed class Error : Throwable() {
        object SettingsUndefinedException : Error()
        object DataUndefinedException : Error()
        object AlreadyStartedException : Error()
        object DataTooLargeException : Error()
        object UnsupportedException : Error()
        object InternalErrorException : Error()
        object TooManyAdvertisersException : Error()

        companion object {
            fun fromCode(code: Int): Error = when (code) {
                ERROR_ALREADY_STARTED -> AlreadyStartedException
                ERROR_DATA_TOO_LARGE -> DataTooLargeException
                ERROR_UNSUPPORTED -> UnsupportedException
                ERROR_TOO_MANY_ADVERTISERS -> TooManyAdvertisersException
                else -> InternalErrorException
            }
        }
    }

    private companion object {
        const val ERROR_ALREADY_STARTED = 3
        const val ERROR_DATA_TOO_LARGE = 1
        const val ERROR_UNSUPPORTED = 5
        const val ERROR_INTERNAL_ERROR = 4
        const val ERROR_TOO_MANY_ADVERTISERS = 2
    }
}
