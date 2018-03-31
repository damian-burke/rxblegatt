package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import com.brainasaservice.rxblegatt.device.RxBleDevice
import java.util.*

interface RxBleCharacteristic {
    val descriptorMap: HashMap<UUID, RxBleDescriptor>

    val characteristic: BluetoothGattCharacteristic

    val uuid: UUID

    val permissions: Int

    val properties: Int

    fun addDescriptor(descriptor: RxBleDescriptor)

    fun removeDescriptor(descriptor: RxBleDescriptor)

    fun enableNotificationSubscription()

    fun disableNotificationSubscription()

    fun hasNotificationSubscriptionEnabled(): Boolean

    fun onDescriptorWriteRequest(device: RxBleDevice, requestId: Int, descriptor: RxBleDescriptor, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?)

    interface Builder {
        fun setUuid(uuid: UUID): Builder

        fun setPermissions(permissions: Int): Builder

        fun setProperties(properties: Int): Builder

        fun addDescriptor(descriptor: RxBleDescriptor): Builder

        fun enableNotificationSubscription(): Builder

        fun build(): RxBleCharacteristic
    }
}
