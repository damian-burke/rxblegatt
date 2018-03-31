package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import com.brainasaservice.rxblegatt.descriptor.RxBleNotificationDescriptor
import io.reactivex.internal.util.BackpressureHelper.add
import java.security.InvalidParameterException
import java.util.*
import kotlin.collections.HashMap

class RxBleCharacteristicImpl(
        override val uuid: UUID,
        override val properties: Int,
        override val permissions: Int
) : RxBleCharacteristic {
    override val characteristic: BluetoothGattCharacteristic = BluetoothGattCharacteristic(
            uuid,
            properties,
            permissions
    )

    override val descriptorMap: HashMap<UUID, RxBleDescriptor> = hashMapOf()

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

    override fun addDescriptor(descriptor: RxBleDescriptor) {
        descriptorMap[descriptor.uuid] = descriptor
        characteristic.addDescriptor(descriptor.descriptor)
    }

    /**
     * TODO: only possible until descriptor has been added to characteristic
     */
    override fun removeDescriptor(descriptor: RxBleDescriptor) {
        descriptorMap.remove(descriptor.uuid)
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

        override fun enableNotificationSubscription(): RxBleCharacteristic.Builder = this.apply {
            descriptors.add(RxBleNotificationDescriptor())
        }

        override fun build(): RxBleCharacteristic {
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
                    permissions!!
            ).apply {
                descriptors.onEach {
                    addDescriptor(it)
                }
            }
        }

    }
}
