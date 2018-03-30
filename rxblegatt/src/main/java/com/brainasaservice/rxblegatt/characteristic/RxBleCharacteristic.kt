package com.brainasaservice.rxblegatt.characteristic

import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor

class RxBleCharacteristic: RxBleCharacteristicInterface {
    val descriptors = mutableListOf<RxBleDescriptor>()

    fun addDescriptor(descriptor: RxBleDescriptor) {
        descriptors.add(descriptor)
        // TODO: add to actual characteristic
    }

    fun removeDescriptor(descriptor: RxBleDescriptor) {
        descriptors.remove(descriptor)
        // TODO: remove from actual characteristic
    }
}
