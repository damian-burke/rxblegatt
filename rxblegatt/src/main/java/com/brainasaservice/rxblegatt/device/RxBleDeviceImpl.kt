package com.brainasaservice.rxblegatt.device

import android.bluetooth.BluetoothDevice
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicReadRequest
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicWriteRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorReadRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorWriteRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import java.util.UUID

class RxBleDeviceImpl(override val device: BluetoothDevice) : RxBleDevice {
    private val statusRelay: PublishRelay<RxBleDevice.Status> = PublishRelay.create()

    private val connectionRelay: BehaviorRelay<RxBleDevice.Connection> = BehaviorRelay.create()

    private val characteristicWriteRequestRelay: PublishRelay<RxBleCharacteristicWriteRequest> = PublishRelay.create()

    private val characteristicReadRequestRelay: PublishRelay<RxBleCharacteristicReadRequest> = PublishRelay.create()

    private val descriptorWriteRequestRelay: PublishRelay<RxBleDescriptorWriteRequest> = PublishRelay.create()

    private val descriptorReadRequestRelay: PublishRelay<RxBleDescriptorReadRequest> = PublishRelay.create()

    private var mtu: Int? = null

    private var connected: Boolean = false

    private val descriptorNotificationSet: HashSet<UUID> = hashSetOf()

    override fun isConnected() = connected

    override fun observeConnection(): Observable<RxBleDevice.Connection> = connectionRelay

    override fun setConnected() {
        connected = true
        connectionRelay.accept(RxBleDevice.Connection.CONNECTED)
    }

    override fun setDisconnected() {
        connected = false
        connectionRelay.accept(RxBleDevice.Connection.DISCONNECTED)
    }

    override fun onNotificationSent() = statusRelay.accept(RxBleDevice.Status.OnNotificationSent)

    override fun onCharacteristicReadRequest(request: RxBleCharacteristicReadRequest) {
        characteristicReadRequestRelay.accept(request)
    }

    override fun onCharacteristicWriteRequest(request: RxBleCharacteristicWriteRequest) {
        characteristicWriteRequestRelay.accept(request)
    }

    override fun onDescriptorWriteRequest(request: RxBleDescriptorWriteRequest) {
        descriptorWriteRequestRelay.accept(request)
    }

    override fun onDescriptorReadRequest(request: RxBleDescriptorReadRequest) {
        descriptorReadRequestRelay.accept(request)
    }

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

    override fun observeStatus(): Observable<RxBleDevice.Status> = statusRelay

    override fun observeCharacteristicReadRequests(): Observable<RxBleCharacteristicReadRequest> {
        return characteristicReadRequestRelay
    }

    override fun observeCharacteristicWriteRequests(): Observable<RxBleCharacteristicWriteRequest> {
        return characteristicWriteRequestRelay
    }

    override fun observeDescriptorReadRequests(): Observable<RxBleDescriptorReadRequest> {
        return descriptorReadRequestRelay
    }

    override fun observeDescriptorWriteRequests(): Observable<RxBleDescriptorWriteRequest> {
        return descriptorWriteRequestRelay
    }
}
