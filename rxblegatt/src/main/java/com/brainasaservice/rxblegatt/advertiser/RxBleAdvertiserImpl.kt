package com.brainasaservice.rxblegatt.advertiser

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import com.brainasaservice.rxblegatt.advertiser.RxBleAdvertiser.Status
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Observable

class RxBleAdvertiserImpl(val bluetoothAdapter: BluetoothAdapter) : RxBleAdvertiser {
    override var settings: AdvertiseSettings? = null

    override var data: AdvertiseData? = null

    override var response: AdvertiseData? = null

    private val statusRelay: PublishRelay<Status> = PublishRelay.create()

    private var callback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            statusRelay.accept(Status.Error(RxBleAdvertiser.Error.fromCode(errorCode)))
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            statusRelay.accept(Status.Active)
        }
    }

    override fun start(): Completable = Completable.fromAction {
        if (settings == null) {
            throw RxBleAdvertiser.Error.SettingsUndefinedException
        }

        if (data == null) {
            throw RxBleAdvertiser.Error.DataUndefinedException
        }

        if (response != null) {
            startWithResponse()
        } else {
            startWithoutResponse()
        }
    }

    override fun stop(): Completable = Completable.fromAction {
        bluetoothAdapter.bluetoothLeAdvertiser.stopAdvertising(callback)
        statusRelay.accept(Status.Inactive)
    }

    override fun status(): Observable<Status> = statusRelay

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
}
