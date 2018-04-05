package com.brainasaservice.rxblegatt.sample

import com.brainasaservice.rxblegatt.device.RxBleDevice
import com.brainasaservice.rxblegatt.message.RxBleData
import com.brainasaservice.rxblegatt.parser.RxBleParser

/**
 * Example implementation of RxBleParser to parse ByteArrays into RxBleData subclasses
 */
class GogoParser : RxBleParser() {
    override fun read(device: RxBleDevice, bytes: ByteArray): RxBleData? {
        if (bytes.size < 2) {
            return null
        }

        val sync = bytes[0].toInt()
        val length = bytes[1].toInt()

        if (bytes.size < 2 + length) {
            return null
        }

        val data = bytes.copyOfRange(2, 2 + length).toString()

        drop(device, 2 + length)

        return GogoMessage(
                sync,
                length,
                data
        )
    }
}
