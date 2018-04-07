package com.brainasaservice.rxblegatt.device

import android.bluetooth.BluetoothDevice
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicReadRequest
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicWriteRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorReadRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorWriteRequest
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.UUID

class RxBleDeviceImpl(override val device: BluetoothDevice) : RxBleDevice {

    private val statusSubject: PublishSubject<RxBleDevice.Status> = PublishSubject.create()

    private val connectionSubject: BehaviorSubject<RxBleDevice.Connection> = BehaviorSubject.create()

    private val characteristicWriteRequestSubject: PublishSubject<RxBleCharacteristicWriteRequest> = PublishSubject.create()

    private val characteristicReadRequestSubject: PublishSubject<RxBleCharacteristicReadRequest> = PublishSubject.create()

    private val descriptorWriteRequestSubject: PublishSubject<RxBleDescriptorWriteRequest> = PublishSubject.create()

    private val descriptorReadRequestSubject: PublishSubject<RxBleDescriptorReadRequest> = PublishSubject.create()

    private var mtu: Int? = null

    private var connected: Boolean = false

    private val descriptorNotificationSet: HashSet<UUID> = hashSetOf()

    override fun isConnected() = connected

    override fun observeConnection(): Observable<RxBleDevice.Connection> = connectionSubject

    override fun setConnected() {
        connected = true
        connectionSubject.onNext(RxBleDevice.Connection.CONNECTED)
    }

    override fun setDisconnected() {
        connected = false
        connectionSubject.onNext(RxBleDevice.Connection.DISCONNECTED)
    }

    override fun onNotificationSent() = statusSubject.onNext(RxBleDevice.Status.OnNotificationSent)

    override fun onCharacteristicReadRequest(request: RxBleCharacteristicReadRequest) {
        characteristicReadRequestSubject.onNext(request)
    }

    override fun onCharacteristicWriteRequest(request: RxBleCharacteristicWriteRequest) {
        characteristicWriteRequestSubject.onNext(request)
    }

    override fun onDescriptorWriteRequest(request: RxBleDescriptorWriteRequest) {
        descriptorWriteRequestSubject.onNext(request)
    }

    override fun onDescriptorReadRequest(request: RxBleDescriptorReadRequest) {
        descriptorReadRequestSubject.onNext(request)
    }

    override fun setNotificationSubscriptionActive(characteristic: RxBleCharacteristic) {
        descriptorNotificationSet.add(characteristic.uuid)
        statusSubject.onNext(RxBleDevice.Status.OnNotificationSubscriptionActive(characteristic))
    }

    override fun setNotificationSubscriptionInactive(characteristic: RxBleCharacteristic) {
        descriptorNotificationSet.remove(characteristic.uuid)
        statusSubject.onNext(RxBleDevice.Status.OnNotificationSubscriptionInactive(characteristic))
    }

    override fun setMtu(mtu: Int) {
        this.mtu = mtu
        statusSubject.onNext(RxBleDevice.Status.OnMtuChanged(mtu))
    }

    override fun observeStatus(): Observable<RxBleDevice.Status> = statusSubject

    override fun observeCharacteristicReadRequests(): Observable<RxBleCharacteristicReadRequest> {
        return characteristicReadRequestSubject
    }

    override fun observeCharacteristicWriteRequests(): Observable<RxBleCharacteristicWriteRequest> {
        return characteristicWriteRequestSubject
    }

    override fun observeDescriptorReadRequests(): Observable<RxBleDescriptorReadRequest> {
        return descriptorReadRequestSubject
    }

    override fun observeDescriptorWriteRequests(): Observable<RxBleDescriptorWriteRequest> {
        return descriptorWriteRequestSubject
    }

    override fun isNotificationSubscriptionActive(characteristic: RxBleCharacteristic): Boolean = descriptorNotificationSet.contains(characteristic.uuid)

    override fun observeNotificationSent(): Observable<RxBleDevice> = statusSubject
            .filter { it == RxBleDevice.Status.OnNotificationSent }
            .map { this }
}
