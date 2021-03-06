package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import io.reactivex.Observable
import java.util.UUID

interface RxBleDescriptor {
    val uuid: UUID

    val descriptor: BluetoothGattDescriptor

    fun onWriteRequest(request: RxBleDescriptorWriteRequest)

    fun onReadRequest(request: RxBleDescriptorReadRequest)

    fun observeWriteRequests(): Observable<RxBleDescriptorWriteRequest>

    fun observeReadRequests(): Observable<RxBleDescriptorReadRequest>

    fun stop()

    interface Builder {
        fun setUuid(uuid: UUID): Builder

        fun setPermissions(permissions: Int): Builder

        fun build(): RxBleDescriptor
    }
}
