package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicReadRequest
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicWriteRequest
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import java.util.*

open class RxBleDescriptorImpl(
        final override val uuid: UUID,
        private val permissions: Int
) : RxBleDescriptor {
    private val descriptorWriteRequestRelay: PublishRelay<RxBleDescriptorWriteRequest> = PublishRelay.create()

    private val descriptorReadRequestRelay: PublishRelay<RxBleDescriptorReadRequest> = PublishRelay.create()

    override val descriptor: BluetoothGattDescriptor = BluetoothGattDescriptor(uuid, permissions)

    override fun observeWriteRequests(): Observable<RxBleDescriptorWriteRequest> = descriptorWriteRequestRelay

    override fun observeReadRequests(): Observable<RxBleDescriptorReadRequest> = descriptorReadRequestRelay

    override fun onReadRequest(request: RxBleDescriptorReadRequest) {
        descriptorReadRequestRelay.accept(request)
    }

    override fun onWriteRequest(request: RxBleDescriptorWriteRequest) {
        descriptorWriteRequestRelay.accept(request)
    }
}
