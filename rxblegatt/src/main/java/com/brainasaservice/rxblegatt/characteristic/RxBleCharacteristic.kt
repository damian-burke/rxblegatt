package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import com.brainasaservice.rxblegatt.device.RxBleDevice
import io.reactivex.Observable
import java.util.UUID

interface RxBleCharacteristic {
    val descriptorMap: HashMap<UUID, RxBleDescriptor>

    val characteristic: BluetoothGattCharacteristic

    val uuid: UUID

    val permissions: Int

    val properties: Int

    fun addDescriptor(descriptor: RxBleDescriptor)

    fun enableNotificationSubscription()

    fun disableNotificationSubscription()

    fun hasNotificationSubscriptionEnabled(): Boolean

    fun onWriteRequest(request: RxBleCharacteristicWriteRequest)

    fun onReadRequest(request: RxBleCharacteristicReadRequest)

    fun observeWriteRequests(): Observable<RxBleCharacteristicWriteRequest>

    interface Builder {
        fun setUuid(uuid: UUID): Builder

        fun setPermissions(permissions: Int): Builder

        fun setProperties(properties: Int): Builder

        fun addDescriptor(descriptor: RxBleDescriptor): Builder

        fun enableNotificationSubscription(): Builder

        fun build(): RxBleCharacteristic
    }
}
