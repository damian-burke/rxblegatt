package com.brainasaservice.rxblegatt.service

import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import java.util.*

class RxBleService(val uuid: UUID, val type: RxBleServiceType = RxBleServiceType.PRIMARY): RxBleServiceInterface {
    private val characteristics = mutableListOf<RxBleCharacteristic>()

    init {

    }

    override fun addCharacteristic(characteristic: RxBleCharacteristic) {
        characteristics.add(characteristic)
        // TODO: add to actual service
    }

    override fun removeCharacteristic(characteristic: RxBleCharacteristic) {
        characteristics.remove(characteristic)
        // TODO: remove from actual service
    }
}
