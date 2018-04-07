package com.brainasaservice.rxblegatt.service

import android.bluetooth.BluetoothGattService
import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicImpl
import java.util.HashMap
import java.util.UUID

class RxBleServiceImpl(
        val uuid: UUID,
        type: RxBleService.Type = RxBleService.Type.PRIMARY,
        private val server: RxBleGattServer
) : RxBleService {

    override var isAdded: Boolean = false

    override val service: BluetoothGattService = BluetoothGattService(uuid, type.value)

    override val characteristicMap: HashMap<UUID, RxBleCharacteristic> = hashMapOf()

    override fun onServiceAdded() {
        isAdded = true
    }

    override fun addCharacteristic(
            uuid: UUID,
            property: Int,
            permission: Int
    ): RxBleCharacteristic {
        val char = RxBleCharacteristicImpl(uuid, property, permission, server)
        return addCharacteristic(char)
    }

    override fun addCharacteristic(block: RxBleCharacteristic.Builder.() -> Unit): RxBleCharacteristic {
        val characteristic = RxBleCharacteristicImpl.Builder().apply(block).build(server)
        return addCharacteristic(characteristic)
    }

    override fun addCharacteristic(characteristic: RxBleCharacteristic): RxBleCharacteristic {
        characteristicMap[characteristic.uuid] = characteristic
        service.addCharacteristic(characteristic.characteristic)
        return characteristic
    }
}
