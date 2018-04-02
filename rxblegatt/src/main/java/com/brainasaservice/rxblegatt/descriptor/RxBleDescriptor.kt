package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import io.reactivex.Observable
import java.util.*

interface RxBleDescriptor {
    val uuid: UUID

    val descriptor: BluetoothGattDescriptor

    fun onWriteRequest(request: RxBleDescriptorWriteRequest)

    fun onReadRequest(request: RxBleDescriptorReadRequest)

    fun observeWriteRequests(): Observable<RxBleDescriptorWriteRequest>

    fun observeReadRequests(): Observable<RxBleDescriptorReadRequest>
}
