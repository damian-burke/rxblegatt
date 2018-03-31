package com.brainasaservice.rxblegatt.characteristic

import android.bluetooth.BluetoothGattCharacteristic
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptor
import java.security.InvalidParameterException
import java.util.*

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

    private val descriptors = mutableListOf<RxBleDescriptor>()

    override fun addDescriptor(descriptor: RxBleDescriptor) {
        descriptors.add(descriptor)
        characteristic.addDescriptor(descriptor.descriptor)
    }

    /**
     * TODO: only possible until descriptor has been added to characteristic
     */
    override fun removeDescriptor(descriptor: RxBleDescriptor) {
        descriptors.remove(descriptor)
    }

    class Builder : RxBleCharacteristic.Builder {
        private var uuid: UUID? = null

        private var properties: Int? = null

        private var permissions: Int? = null

        private val descriptors = mutableListOf<RxBleDescriptor>()

        override fun setUuid(uuid: UUID): RxBleCharacteristic.Builder {
            this.uuid = uuid
            return this
        }

        override fun setPermissions(permissions: Int): RxBleCharacteristic.Builder {
            this.permissions = permissions
            return this
        }

        override fun setProperties(properties: Int): RxBleCharacteristic.Builder {
            this.properties = properties
            return this
        }

        override fun addDescriptor(descriptor: RxBleDescriptor): RxBleCharacteristic.Builder {
            descriptors.add(descriptor)
            return this
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
