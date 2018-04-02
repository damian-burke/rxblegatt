package com.brainasaservice.rxblegatt

import com.brainasaservice.rxblegatt.device.RxBleDevice

data class RxBleResponse(
        val device: RxBleDevice,
        val requestId: Int,
        val status: Int,
        val offset: Int,
        val value: ByteArray?
)
