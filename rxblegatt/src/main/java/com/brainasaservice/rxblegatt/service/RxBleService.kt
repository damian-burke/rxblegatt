package com.brainasaservice.rxblegatt.service

import android.bluetooth.BluetoothGattService
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicImpl
import java.util.*

interface RxBleService {
    val service: BluetoothGattService

    val characteristicMap: HashMap<UUID, RxBleCharacteristic>

    fun removeCharacteristic(characteristic: RxBleCharacteristic)

    fun removeCharacteristic(uuid: UUID)

    fun addCharacteristic(uuid: UUID, property: Int, permission: Int): RxBleCharacteristic

    fun addCharacteristic(characteristic: RxBleCharacteristic)

    enum class Type(val value: Int) {
        PRIMARY(BluetoothGattService.SERVICE_TYPE_PRIMARY),
        SECONDARY(BluetoothGattService.SERVICE_TYPE_SECONDARY)
    }
}
