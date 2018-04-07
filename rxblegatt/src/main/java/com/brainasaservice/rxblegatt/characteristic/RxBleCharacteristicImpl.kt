package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorImpl
import com.brainasaservice.rxblegatt.descriptor.RxBleNotificationDescriptor
import com.brainasaservice.rxblegatt.device.RxBleDevice
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.security.InvalidParameterException
import java.util.UUID

class RxBleCharacteristicImpl(
        override val uuid: UUID,
        override val properties: Int,
        override val permissions: Int,
        private val server: RxBleGattServer
) : RxBleCharacteristic {

    private val subscribedDevices: List<RxBleDevice> = mutableListOf()

    private val writeRequestSubject: PublishSubject<RxBleCharacteristicWriteRequest> = PublishSubject.create()

    private val readRequestSubject: PublishSubject<RxBleCharacteristicReadRequest> = PublishSubject.create()

    override val characteristic: BluetoothGattCharacteristic = BluetoothGattCharacteristic(
            uuid,
            properties,
            permissions
    )

    override fun observeWriteRequests(): Observable<RxBleCharacteristicWriteRequest> = writeRequestSubject

    override val descriptorMap: HashMap<UUID, RxBleDescriptor> = hashMapOf()

    override fun onWriteRequest(request: RxBleCharacteristicWriteRequest) {
        writeRequestSubject.onNext(request)
    }

    override fun setValue(bytes: ByteArray, notifySubscribers: Boolean): Completable = server.deviceList()
            .doOnSubscribe { characteristic.value = bytes }
            .first(emptyList())
            .filter { notifySubscribers }
            .flatMapObservable { Observable.fromIterable(it) }
            .filter { it.isNotificationSubscriptionActive(this) }
            .flatMapCompletable {
                server.notifyCharacteristicChanged(it, this)
            }

    override fun onReadRequest(request: RxBleCharacteristicReadRequest) {
        readRequestSubject.onNext(request)
    }

    override fun enableNotificationSubscription() {
        val descriptor = RxBleNotificationDescriptor()
        descriptorMap[descriptor.uuid] = descriptor
    }

    override fun hasNotificationSubscriptionEnabled() = descriptorMap.any {
        it.value is RxBleNotificationDescriptor
    }

    /**
     * TODO: only possible until descriptor has been added to characteristic
     */
    override fun disableNotificationSubscription() {
        descriptorMap.filter { it.value is RxBleNotificationDescriptor }.onEach {
            descriptorMap.remove(it.key)
        }
    }

    override fun addDescriptor(descriptor: RxBleDescriptor): RxBleDescriptor {
        descriptorMap[descriptor.uuid] = descriptor
        characteristic.addDescriptor(descriptor.descriptor)
        return descriptor
    }

    override fun addDescriptor(block: RxBleDescriptor.Builder.() -> Unit): RxBleDescriptor {
        val descriptor = RxBleDescriptorImpl.Builder().apply(block).build()
        return addDescriptor(descriptor)
    }

    class Builder : RxBleCharacteristic.Builder {
        private var uuid: UUID? = null

        private var properties: Int? = null

        private var permissions: Int? = null

        private val descriptors = mutableListOf<RxBleDescriptor>()

        override fun setUuid(uuid: UUID): RxBleCharacteristic.Builder = this.apply {
            this.uuid = uuid
        }

        override fun setPermissions(permissions: Int): RxBleCharacteristic.Builder = this.apply {
            this.permissions = permissions
        }

        override fun setProperties(properties: Int): RxBleCharacteristic.Builder = this.apply {
            this.properties = properties
        }

        override fun addDescriptor(descriptor: RxBleDescriptor): RxBleCharacteristic.Builder = this.apply {
            descriptors.add(descriptor)
        }

        override fun addDescriptor(block: RxBleDescriptor.Builder.() -> Unit): Builder = this.apply {
            val descriptor = RxBleDescriptorImpl.Builder().apply(block).build()
            descriptors.add(descriptor)
        }

        override fun enableNotificationSubscription(): RxBleCharacteristic.Builder = this.apply {
            descriptors.add(RxBleNotificationDescriptor())
        }

        override fun build(server: RxBleGattServer): RxBleCharacteristic {
            if (uuid == null) {
                throw InvalidParameterException("UUID must be set.")
            }

            if (properties == null) {
                throw InvalidParameterException("Properties must be set.")
            }

            if (permissions == null) {
                throw InvalidParameterException("Permissions must be set.")
            }

            return RxBleCharacteristicImpl(
                    uuid!!,
                    properties!!,
                    permissions!!,
                    server
            ).apply {
                descriptors.onEach {
                    addDescriptor(it)
                }
            }
        }

    }
}
