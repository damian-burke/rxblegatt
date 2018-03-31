package com.brainasaservice.rxblegatt.service

import android.bluetooth.BluetoothGattService
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicImpl
import java.util.*

class RxBleServiceImpl(
        val uuid: UUID,
        private val type: RxBleService.Type = RxBleService.Type.PRIMARY
) : RxBleService {

    override val service: BluetoothGattService = BluetoothGattService(uuid, type.value)

    override val characteristicMap: HashMap<UUID, RxBleCharacteristic> = hashMapOf()

    override fun addCharacteristic(
            uuid: UUID,
            property: Int,
            permission: Int
    ): RxBleCharacteristic {
        val char = RxBleCharacteristicImpl(uuid, property, permission)
        characteristicMap[uuid] = char
        service.addCharacteristic(char.characteristic)
        return char
    }

    override fun addCharacteristic(characteristic: RxBleCharacteristic) {
        characteristicMap[characteristic.uuid] = characteristic
        service.addCharacteristic(characteristic.characteristic)
    }

    /**
     * TODO: only possible until service has been added to server
     */
    override fun removeCharacteristic(characteristic: RxBleCharacteristic) {
        characteristicMap.remove(characteristic.uuid)
    }

    /**
     * TODO: only possible until service has been added to server
     */
    override fun removeCharacteristic(uuid: UUID) {
        characteristicMap.remove(uuid)
    }
}
