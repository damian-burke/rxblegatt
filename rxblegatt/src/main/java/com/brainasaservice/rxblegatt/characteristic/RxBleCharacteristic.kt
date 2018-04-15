package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.UUID

interface RxBleCharacteristic {
    val characteristic: BluetoothGattCharacteristic

    val uuid: UUID

    val permissions: Int

    val properties: Int

    fun getDescriptor(uuid: UUID): RxBleDescriptor?

    fun setValue(bytes: ByteArray, notifySubscribers: Boolean = true, ignoreMtu: Boolean = false): Completable

    fun addDescriptor(descriptor: RxBleDescriptor): RxBleDescriptor

    fun addDescriptor(block: RxBleDescriptor.Builder.() -> Unit): RxBleDescriptor

    fun enableNotificationSubscription()

    fun disableNotificationSubscription()

    fun hasNotificationSubscriptionEnabled(): Boolean

    fun onWriteRequest(request: RxBleCharacteristicWriteRequest)

    fun onReadRequest(request: RxBleCharacteristicReadRequest)

    fun observeWriteRequests(): Observable<RxBleCharacteristicWriteRequest>

    fun stop()

    interface Builder {
        fun setUuid(uuid: UUID): Builder

        fun setPermissions(permissions: Int): Builder

        fun setProperties(properties: Int): Builder

        fun addDescriptor(descriptor: RxBleDescriptor): Builder

        fun addDescriptor(block: RxBleDescriptor.Builder.() -> Unit): Builder

        fun enableNotificationSubscription(): Builder

        fun build(server: RxBleGattServer): RxBleCharacteristic
    }
}
