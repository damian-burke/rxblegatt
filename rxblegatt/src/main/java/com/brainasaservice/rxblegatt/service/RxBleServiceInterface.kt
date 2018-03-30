package com.brainasaservice.rxblegatt.service

import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic

internal interface RxBleServiceInterface {
    fun addCharacteristic(characteristic: RxBleCharacteristic)

    fun removeCharacteristic(characteristic: RxBleCharacteristic)
}