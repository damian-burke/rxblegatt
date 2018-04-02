package com.brainasaservice.rxblegatt.descriptor

import com.brainasaservice.rxblegatt.device.RxBleDevice
import java.util.Arrays

data class RxBleDescriptorWriteRequest(
        val device: RxBleDevice,
        val descriptor: RxBleDescriptor,
        val requestId: Int,
        val preparedWrite: Boolean,
        val responseNeeded: Boolean,
        val offset: Int,
        val value: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RxBleDescriptorWriteRequest

        if (device != other.device) return false
        if (requestId != other.requestId) return false
        if (descriptor != other.descriptor) return false
        if (preparedWrite != other.preparedWrite) return false
        if (responseNeeded != other.responseNeeded) return false
        if (offset != other.offset) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + requestId
        result = 31 * result + descriptor.hashCode()
        result = 31 * result + preparedWrite.hashCode()
        result = 31 * result + responseNeeded.hashCode()
        result = 31 * result + offset
        result = 31 * result + (value?.let { Arrays.hashCode(it) } ?: 0)
        return result
    }
}
