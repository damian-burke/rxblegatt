package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.RxBleGattServerStatus
import com.brainasaservice.rxblegatt.device.RxBleDevice
import java.util.Arrays

data class RxBleCharacteristicReadRequest(
        val server: RxBleGattServer,
        val device: RxBleDevice,
        val characteristic: RxBleCharacteristic,
        val requestId: Int,
        val offset: Int
)
