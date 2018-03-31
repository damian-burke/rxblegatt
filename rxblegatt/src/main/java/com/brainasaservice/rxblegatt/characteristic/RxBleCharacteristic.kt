package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import java.util.*

interface RxBleCharacteristic {
    val characteristic: BluetoothGattCharacteristic

    val uuid: UUID

    val permissions: Int

    val properties: Int

    fun addDescriptor(descriptor: RxBleDescriptor)

    fun removeDescriptor(descriptor: RxBleDescriptor)

    interface Builder {
        fun setUuid(uuid: UUID): Builder

        fun setPermissions(permissions: Int): Builder

        fun setProperties(properties: Int): Builder

        fun addDescriptor(descriptor: RxBleDescriptor): Builder

        fun build(): RxBleCharacteristic
    }
}
