package com.brainasaservice.rxblegatt.device

import android.bluetooth.BluetoothDevice
import io.reactivex.Observable

interface RxBleDevice {
    val device: BluetoothDevice

    fun status(): Observable<RxBleDevice.Status>

    fun setMtu(mtu: Int)

    fun notificationSent()

    sealed class Status {
        data class OnMtuChanged(val mtu: Int) : Status()
        object OnNotificationSent: Status()
    }
}
