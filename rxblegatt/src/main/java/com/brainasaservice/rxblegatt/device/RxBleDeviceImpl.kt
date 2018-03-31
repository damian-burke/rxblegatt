package com.brainasaservice.rxblegatt.device

import android.bluetooth.BluetoothDevice
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class RxBleDeviceImpl(override val device: BluetoothDevice) : RxBleDevice {
    private val statusRelay: PublishRelay<RxBleDevice.Status> = PublishRelay.create()

    private var mtu: Int? = null

    override fun notificationSent() = statusRelay.accept(RxBleDevice.Status.OnNotificationSent)

    override fun setMtu(mtu: Int) {
        this.mtu = mtu
        statusRelay.accept(RxBleDevice.Status.OnMtuChanged(mtu))
    }

    override fun status(): Observable<RxBleDevice.Status> = statusRelay
}
