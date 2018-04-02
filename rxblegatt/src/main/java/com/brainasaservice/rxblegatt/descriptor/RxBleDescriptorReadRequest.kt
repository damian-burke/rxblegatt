package com.brainasaservice.rxblegatt.descriptor

import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.device.RxBleDevice
import java.util.Arrays

data class RxBleDescriptorReadRequest(
        val server: RxBleGattServer,
        val device: RxBleDevice,
        val descriptor: RxBleDescriptor,
        val requestId: Int,
        val offset: Int
)
