package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID

class RxBleNotificationDescriptor : RxBleDescriptorImpl(
        UUID.fromString(DESCRIPTOR_UUID),
        PERMISSIONS
) {
    private companion object {
        const val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"
        const val PERMISSIONS = BluetoothGattDescriptor.PERMISSION_WRITE or BluetoothGattDescriptor.PERMISSION_READ
    }
}