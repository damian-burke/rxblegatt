package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import java.util.*

interface RxBleDescriptor {
    val uuid: UUID

    val descriptor: BluetoothGattDescriptor
}
