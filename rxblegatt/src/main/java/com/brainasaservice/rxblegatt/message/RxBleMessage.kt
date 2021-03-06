package com.brainasaservice.rxblegatt.message

import com.brainasaservice.rxblegatt.device.RxBleDevice

data class RxBleMessage(
        val message: RxBleData,
        val sender: RxBleDevice,
        val timestamp: Long = System.currentTimeMillis()
)
