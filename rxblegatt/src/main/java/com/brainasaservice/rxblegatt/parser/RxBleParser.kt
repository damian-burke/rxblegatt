package com.brainasaservice.rxblegatt.parser

import com.brainasaservice.rxblegatt.device.RxBleDevice
import com.brainasaservice.rxblegatt.message.RxBleData
import io.reactivex.Observable

abstract class RxBleParser {
    private val deviceBytesMap = hashMapOf<RxBleDevice, ByteArray>()

    fun parse(device: RxBleDevice, bytes: ByteArray): Observable<RxBleData> {
        val pool = deviceBytesMap.getOrPut(device, { ByteArray(0) })
        return read(device, pool)?.let { Observable.just(it) } ?: Observable.never()
    }

    /**
     * Clears the byte buffer for either one connected device or for all devices.
     * @param device Device to specify the buffer, otherwise all buffers will be cleared
     */
    fun clear(device: RxBleDevice? = null) {
        if (device != null) {
            deviceBytesMap[device] = ByteArray(0)
        } else {
            deviceBytesMap.clear()
        }
    }

    /**
     * Drops a specified amount of bytes from the device's byte buffer.
     * @param device Device to specify the buffer
     * @param byteCount Amount of bytes to drop (starting at index 0)
     */
    fun drop(device: RxBleDevice, byteCount: Int = 1) {
        deviceBytesMap[device]?.drop(byteCount)?.toByteArray()?.let {
            deviceBytesMap[device] = it
        }
    }

    /**
     * Mapping a byte array to the desired data object. If there are not enough bytes
     * in the pool, it might return null to avoid triggering an emission in the Observable.
     *
     * An implementation of this method should also call "drop" to remove the parsed bytes from
     * the buffer.
     *
     * @param device Device that sent the available data
     * @param bytes Byte buffer available to parse
     *
     */
    abstract fun read(device: RxBleDevice, bytes: ByteArray): RxBleData?
}
