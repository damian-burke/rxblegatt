package com.brainasaservice.rxblegatt.characteristic

import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor

interface RxBleCharacteristic {
    fun addDescriptor(descriptor: RxBleDescriptor)

    fun removeDescriptor(descriptor: RxBleDescriptor)
}
