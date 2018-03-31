package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import java.util.*

class RxBleDescriptorImpl(
        override val uuid: UUID,
        private val permissions: Int
) : RxBleDescriptor {
    override val descriptor: BluetoothGattDescriptor = BluetoothGattDescriptor(uuid, permissions)

}
