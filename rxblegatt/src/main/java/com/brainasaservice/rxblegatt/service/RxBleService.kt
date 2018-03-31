package com.brainasaservice.rxblegatt.service

import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicImpl
import java.util.*

class RxBleService(
        val uuid: UUID,
        val type: RxBleServiceType = RxBleServiceType.PRIMARY
): RxBleServiceInterface {
    private val characteristics = mutableListOf<RxBleCharacteristicImpl>()

    init {

    }

    override fun addCharacteristic(characteristic: RxBleCharacteristicImpl) {
        characteristics.add(characteristic)
        /**
         * TODO: add to actual service
         */
    }

    override fun removeCharacteristic(characteristic: RxBleCharacteristicImpl) {
        characteristics.remove(characteristic)
        /**
         * TODO: remove from actual service
         */
    }
}
