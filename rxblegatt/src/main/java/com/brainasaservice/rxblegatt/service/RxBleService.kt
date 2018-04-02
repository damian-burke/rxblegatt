package com.brainasaservice.rxblegatt.service

import android.bluetooth.BluetoothGattService
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicImpl
import java.util.*

interface RxBleService {
    val service: BluetoothGattService

    val characteristicMap: HashMap<UUID, RxBleCharacteristic>

    var isAdded: Boolean

    fun addCharacteristic(uuid: UUID, property: Int, permission: Int): RxBleCharacteristic

    fun addCharacteristic(characteristic: RxBleCharacteristic): RxBleCharacteristic

    fun addCharacteristic(block: RxBleCharacteristic.Builder.() -> Unit): RxBleCharacteristic

    fun onServiceAdded()

    enum class Type(val value: Int) {
        PRIMARY(BluetoothGattService.SERVICE_TYPE_PRIMARY),
        SECONDARY(BluetoothGattService.SERVICE_TYPE_SECONDARY)
    }
}
