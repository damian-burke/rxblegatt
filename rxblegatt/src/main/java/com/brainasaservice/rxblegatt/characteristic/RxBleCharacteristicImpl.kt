package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorImpl
import com.brainasaservice.rxblegatt.descriptor.RxBleNotificationDescriptor
import com.brainasaservice.rxblegatt.device.RxBleDevice
import com.brainasaservice.rxblegatt.util.MainThreadExecutor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.security.InvalidParameterException
import java.util.UUID

class RxBleCharacteristicImpl(
        override val uuid: UUID,
        override val properties: Int,
        override val permissions: Int,
        override val scheduler: Scheduler = Schedulers.from(MainThreadExecutor()),
        private val server: RxBleGattServer
) : RxBleCharacteristic {

    private val subscribedDevices: MutableList<RxBleDevice> = mutableListOf()

    private val writeRequestSubject: PublishSubject<RxBleCharacteristicWriteRequest> = PublishSubject.create()

    private val readRequestSubject: PublishSubject<RxBleCharacteristicReadRequest> = PublishSubject.create()

    private val descriptorMap: HashMap<UUID, RxBleDescriptor> = hashMapOf()

    override val characteristic: BluetoothGattCharacteristic = BluetoothGattCharacteristic(
            uuid,
            properties,
            permissions
    )

    override fun getDescriptor(uuid: UUID): RxBleDescriptor? = descriptorMap[uuid]

    override fun observeWriteRequests(): Observable<RxBleCharacteristicWriteRequest> = writeRequestSubject

    override fun onWriteRequest(request: RxBleCharacteristicWriteRequest) {
        writeRequestSubject.onNext(request)
    }

    override fun stop() {
        subscribedDevices.clear()
        descriptorMap.values.onEach { it.stop() }
        writeRequestSubject.onComplete()
        readRequestSubject.onComplete()
    }

    override fun setValue(bytes: ByteArray, notifySubscribers: Boolean, ignoreMtu: Boolean): Completable = server.deviceList()
            .doOnSubscribe { println("*** setValue(bytes=${bytes.joinToString(", ")}.subscribe()") }
            .first(emptyList())
            .flatMapObservable { devices ->
                Single.fromCallable {
                    if (ignoreMtu) {
                        bytes.size
                    } else {
                        devices.minBy { device -> device.mtu }?.mtu ?: RxBleDevice.DEFAULT_MTU
                    }
                }
                        .flatMapObservable { minMtu ->
                            Observable.fromIterable(bytes.asIterable())
                                    .buffer(minMtu)
                        }
                        .map { it.toByteArray() }
                        .doOnNext { characteristic.value = bytes }
                        .map { devices }
            }
            .filter { notifySubscribers }
            .flatMap { Observable.fromIterable(it) }
            .filter { it.isNotificationSubscriptionActive(this) }
            .flatMapCompletable {
                server.notifyCharacteristicChanged(it, this)
            }
            .subscribeOn(scheduler)

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

        private var scheduler: Scheduler = Schedulers.from(MainThreadExecutor())

        private val descriptors = mutableListOf<RxBleDescriptor>()

        override fun setScheduler(scheduler: Scheduler): RxBleCharacteristic.Builder = this.apply {
            this.scheduler = scheduler
        }

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
                    scheduler,
                    server
            ).apply {
                descriptors.onEach {
                    addDescriptor(it)
                }
            }
        }

    }
}
