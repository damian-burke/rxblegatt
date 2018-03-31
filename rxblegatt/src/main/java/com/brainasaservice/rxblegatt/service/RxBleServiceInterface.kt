package com.brainasaservice.rxblegatt.service

import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicImpl

internal interface RxBleServiceInterface {
    fun addCharacteristic(characteristic: RxBleCharacteristicImpl)

    fun removeCharacteristic(characteristic: RxBleCharacteristicImpl)
}
