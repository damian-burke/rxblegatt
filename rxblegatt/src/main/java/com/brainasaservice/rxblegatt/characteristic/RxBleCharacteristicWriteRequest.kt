package com.brainasaservice.rxblegatt.characteristic

import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.device.RxBleDevice
import java.util.Arrays

data class RxBleCharacteristicWriteRequest(
        val server: RxBleGattServer,
        val device: RxBleDevice,
        val characteristic: RxBleCharacteristic,
        val requestId: Int,
        val preparedWrite: Boolean,
        val responseNeeded: Boolean,
        val offset: Int,
        val value: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RxBleCharacteristicWriteRequest

        if (device != other.device) return false
        if (requestId != other.requestId) return false
        if (characteristic != other.characteristic) return false
        if (preparedWrite != other.preparedWrite) return false
        if (responseNeeded != other.responseNeeded) return false
        if (offset != other.offset) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + requestId
        result = 31 * result + (characteristic.hashCode())
        result = 31 * result + preparedWrite.hashCode()
        result = 31 * result + responseNeeded.hashCode()
        result = 31 * result + offset
        result = 31 * result + (value?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}