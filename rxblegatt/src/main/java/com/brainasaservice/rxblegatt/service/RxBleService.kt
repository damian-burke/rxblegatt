package com.brainasaservice.rxblegatt.service

import android.bluetooth.BluetoothGattService
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import io.reactivex.Observable
import java.util.UUID

interface RxBleService {
    val service: BluetoothGattService

    var isAdded: Boolean

    fun observeCharacteristics(): Observable<RxBleCharacteristic>

    fun observeCharacteristicList(): Observable<List<RxBleCharacteristic>>

    fun addCharacteristic(
            uuid: UUID,
            property: Int,
            permission: Int
    ): RxBleCharacteristic

    fun addCharacteristic(characteristic: RxBleCharacteristic): RxBleCharacteristic

    fun addCharacteristic(block: RxBleCharacteristic.Builder.() -> Unit): RxBleCharacteristic

    fun getCharacteristic(uuid: UUID): RxBleCharacteristic?

    fun onServiceAdded()

    enum class Type(val value: Int) {
        PRIMARY(BluetoothGattService.SERVICE_TYPE_PRIMARY),
        SECONDARY(BluetoothGattService.SERVICE_TYPE_SECONDARY)
    }
}
