package com.brainasaservice.rxblegatt.device

import android.bluetooth.BluetoothDevice
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import io.reactivex.Observable

interface RxBleDevice {
    val device: BluetoothDevice

    fun status(): Observable<RxBleDevice.Status>

    fun setMtu(mtu: Int)

    fun notificationSent()

    fun notificationSubscriptionActive(characteristic: RxBleCharacteristic)

    fun notificationSubscriptionInactive(characteristic: RxBleCharacteristic)

    sealed class Status {
        data class OnMtuChanged(val mtu: Int) : Status()
        data class OnNotificationSubscriptionActive(val characteristic: RxBleCharacteristic) : Status()
        data class OnNotificationSubscriptionInactive(val characteristic: RxBleCharacteristic) : Status()
        object OnNotificationSent : Status()
    }
}
