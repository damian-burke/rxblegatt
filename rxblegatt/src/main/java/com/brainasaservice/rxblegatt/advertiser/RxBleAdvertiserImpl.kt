package com.brainasaservice.rxblegatt.advertiser

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
import android.bluetooth.le.PeriodicAdvertisingParameters
import android.os.Build
import com.brainasaservice.rxblegatt.advertiser.RxBleAdvertiser.Status
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RxBleAdvertiserImpl(
        private val bluetoothAdapter: BluetoothAdapter
) : RxBleAdvertiser {
    private val statusSubject: PublishSubject<Status> = PublishSubject.create()

    private var callback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            statusSubject.onNext(Status.Error(RxBleAdvertiser.Error.fromCode(errorCode)))
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            statusSubject.onNext(Status.Active)
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    private var setCallback: AdvertisingSetCallback = object : AdvertisingSetCallback() {
        override fun onAdvertisingSetStarted(advertisingSet: AdvertisingSet?, txPower: Int, status: Int) {
            super.onAdvertisingSetStarted(advertisingSet, txPower, status)

            val exception = RxBleAdvertiser.Error.fromCode(status)

            when (status) {
                AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED,
                AdvertisingSetCallback.ADVERTISE_SUCCESS -> statusSubject.onNext(Status.Active)
                else -> statusSubject.onNext(Status.Error(exception))
            }
        }
    }

    override var settings: AdvertiseSettings? = null

    override var data: AdvertiseData? = null

    override var response: AdvertiseData? = null

    override var parameters: AdvertisingSetParameters? = null

    override var periodicAdvertisingParameters: PeriodicAdvertisingParameters? = null

    override var periodicData: AdvertiseData? = null

    override var enableAdvertisingSet: Boolean = true

    override fun start(): Completable = Completable.fromAction {
        checkValidSettings()

        if (data == null) {
            throw RxBleAdvertiser.Error.DataUndefinedException
        }

        if (useAdvertiseSet()) {
            startAdvertisingSet()
        } else {
            if (response != null) {
                startWithResponse()
            } else {
                startWithoutResponse()
            }
        }
    }

    private fun checkValidSettings() {
        if (settings == null && parameters == null) {
            throw RxBleAdvertiser.Error.SettingsUndefinedException
        }

        if (settings == null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                throw RxBleAdvertiser.Error.MinSdkRequirementNotFulfilledException
            }
        }
    }

    private fun useAdvertiseSet(): Boolean {
        val isSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        val hasParameters = parameters != null

        return enableAdvertisingSet && isSupported && hasParameters
    }

    override fun stop(): Completable = Completable.fromAction {
        bluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(callback)
        statusSubject.onNext(Status.Inactive)
        statusSubject.onComplete()
    }

    override fun status(): Observable<Status> = statusSubject

    /**
     * Start advertising with defined response data.
     */
    private fun startWithResponse() {
        bluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(
                settings,
                data,
                response,
                callback
        )
    }

    /**
     * Start advertising without response data.
     */
    private fun startWithoutResponse() {
        bluetoothAdapter.bluetoothLeAdvertiser.startAdvertising(
                settings,
                data,
                callback
        )
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun startAdvertisingSet() {
        bluetoothAdapter.bluetoothLeAdvertiser.startAdvertisingSet(
                parameters,
                data,
                response,
                periodicAdvertisingParameters,
                periodicData,
                setCallback
        )
    }
}
