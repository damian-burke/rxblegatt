package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import java.util.*

class RxBleNotificationDescriptor(override val uuid: UUID = UUID.fromString(DESCRIPTOR_UUID)) : RxBleDescriptor {
    override val descriptor: BluetoothGattDescriptor = BluetoothGattDescriptor(
            uuid,
            BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ
    ).apply {
        value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    }

    private companion object {
        const val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"
    }
}