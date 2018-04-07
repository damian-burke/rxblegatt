package com.brainasaservice.rxblegatt.descriptor

import android.bluetooth.BluetoothGattDescriptor
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.security.InvalidParameterException
import java.util.UUID

open class RxBleDescriptorImpl(
        final override val uuid: UUID,
        private val permissions: Int
) : RxBleDescriptor {
    private val descriptorWriteRequestSubject: PublishSubject<RxBleDescriptorWriteRequest> = PublishSubject.create()

    private val descriptorReadRequestSubject: PublishSubject<RxBleDescriptorReadRequest> = PublishSubject.create()

    override val descriptor: BluetoothGattDescriptor = BluetoothGattDescriptor(uuid, permissions)

    override fun observeWriteRequests(): Observable<RxBleDescriptorWriteRequest> = descriptorWriteRequestSubject

    override fun observeReadRequests(): Observable<RxBleDescriptorReadRequest> = descriptorReadRequestSubject

    override fun onReadRequest(request: RxBleDescriptorReadRequest) {
        descriptorReadRequestSubject.onNext(request)
    }

    override fun onWriteRequest(request: RxBleDescriptorWriteRequest) {
        descriptorWriteRequestSubject.onNext(request)
    }

    class Builder : RxBleDescriptor.Builder {
        private var uuid: UUID? = null
        private var permissions: Int? = null

        override fun setUuid(uuid: UUID): RxBleDescriptor.Builder = this.apply {
            this.uuid = uuid
        }

        override fun setPermissions(permissions: Int): RxBleDescriptor.Builder = this.apply {
            this.permissions = permissions
        }

        override fun build(): RxBleDescriptor {
            if (uuid == null) {
                throw InvalidParameterException("UUID must be set.")
            }

            if (permissions == null) {
                throw InvalidParameterException("Permissions must be set.")
            }

            return RxBleDescriptorImpl(
                    uuid!!,
                    permissions!!
            )
        }
    }
}
