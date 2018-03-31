package com.brainasaservice.rxblegatt.characteristic

import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorImpl

class RxBleCharacteristicImpl: RxBleCharacteristic {
    val descriptors = mutableListOf<RxBleDescriptor>()

    override fun addDescriptor(descriptor: RxBleDescriptor) {
        descriptors.add(descriptor)
        // TODO: add to actual characteristic
    }

    override fun removeDescriptor(descriptor: RxBleDescriptor) {
        descriptors.remove(descriptor)
        // TODO: remove from actual characteristic
    }
}
