package com.brainasaservice.rxblegatt.device

import android.bluetooth.BluetoothDevice
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import java.util.*
import kotlin.collections.HashSet

class RxBleDeviceImpl(override val device: BluetoothDevice) : RxBleDevice {
    private val statusRelay: PublishRelay<RxBleDevice.Status> = PublishRelay.create()

    private var mtu: Int? = null

    private val descriptorNotificationSet: HashSet<UUID> = hashSetOf()

    override fun notificationSent() = statusRelay.accept(RxBleDevice.Status.OnNotificationSent)

    override fun notificationSubscriptionActive(characteristic: RxBleCharacteristic) {
        descriptorNotificationSet.add(characteristic.uuid)
        statusRelay.accept(RxBleDevice.Status.OnNotificationSubscriptionActive(characteristic))
    }

    override fun notificationSubscriptionInactive(characteristic: RxBleCharacteristic) {
        descriptorNotificationSet.remove(characteristic.uuid)
        statusRelay.accept(RxBleDevice.Status.OnNotificationSubscriptionInactive(characteristic))
    }

    override fun setMtu(mtu: Int) {
        this.mtu = mtu
        statusRelay.accept(RxBleDevice.Status.OnMtuChanged(mtu))
    }

    override fun status(): Observable<RxBleDevice.Status> = statusRelay
}
