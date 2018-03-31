package com.brainasaservice.rxblegatt

import android.bluetooth.*
import android.content.ContentValues.TAG
import android.content.Context
import com.brainasaservice.rxblegatt.advertiser.RxBleAdvertiser
import com.brainasaservice.rxblegatt.advertiser.RxBleAdvertiserImpl
import com.brainasaservice.rxblegatt.descriptor.RxBleNotificationDescriptor
import com.brainasaservice.rxblegatt.device.RxBleDevice
import com.brainasaservice.rxblegatt.device.RxBleDeviceImpl
import com.brainasaservice.rxblegatt.service.RxBleService
import com.brainasaservice.rxblegatt.util.Logger
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.*
import kotlin.collections.HashMap

class RxBleGattServer(private val context: Context) {
    val advertiser: RxBleAdvertiser by lazy {
        RxBleAdvertiserImpl(bluetoothAdapter)
    }

    private var server: BluetoothGattServer? = null

    private val deviceMap: HashMap<String, RxBleDevice> = hashMapOf()

    private val serviceMap: HashMap<UUID, RxBleService> = hashMapOf()

    private val statusRelay: PublishRelay<RxBleGattServerStatus> = PublishRelay.create()

    private val deviceListRelay: BehaviorRelay<List<RxBleDevice>> = BehaviorRelay.create()

    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val serverCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            device?.let { dev ->
                when (newState) {
                    BluetoothGattServer.STATE_CONNECTED -> handleDeviceConnected(dev)
                    BluetoothGattServer.STATE_DISCONNECTED -> handleDeviceDisconnected(dev)
                }
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            /**
             * TODO: update RxBleService
             */
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            /**
             * TODO: update RxBleDescriptor
             */
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            /**
             * TODO: update RxBleDescriptor
             */
            descriptor?.let {
                val serviceUuid = it.characteristic.service.uuid
                val charUuid = it.characteristic.uuid
                val descUuid = it.uuid

                val rxDescriptor = serviceMap[serviceUuid]?.characteristicMap?.get(charUuid)?.descriptorMap?.get(descUuid)
                val rxCharacteristic = serviceMap[serviceUuid]?.characteristicMap?.get(charUuid)
                val rxDevice = deviceMap[device?.address]

                if (rxDevice != null && rxCharacteristic != null && rxDescriptor != null) {
                    /**
                     * We'll only proceed, if we've identified
                     * - device
                     * - characteristic
                     * - descriptor
                     */
                    if (rxDescriptor is RxBleNotificationDescriptor) {
                        /**
                         * If the current descriptor is a Notification descriptor, set subscription to active
                         * TODO: update the descriptor / characteristic with subscription?
                         */
                        rxDevice.notificationSubscriptionActive(rxCharacteristic)
                    } else {
                        /**
                         * Otherwise, forward the write request to the RxBleCharacteristic
                         * TODO: update device with write request?
                         */
                        rxCharacteristic.onDescriptorWriteRequest(rxDevice, requestId, rxDescriptor, preparedWrite, responseNeeded, offset, value)
                    }
                }
            }
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            device?.let {
                deviceMap[it.address]?.setMtu(mtu)
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            /**
             * TODO: update RxBleCharacteristic
             */
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            /**
             * TODO: update RxBleCharacteristic
             */
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            device?.let {
                deviceMap[it.address]?.notificationSent()
            }
        }
    }

    fun start(): Completable = Completable.fromAction {
        if (server != null) {
            throw Error.ServerAlreadyOpenException
        }

        if (!bluetoothAdapter.isEnabled) {
            throw Error.BluetoothDisabledException
        }

        if (!isPeripheralModeSupported(bluetoothAdapter)) {
            throw Error.NotSupportedException
        }

        server = bluetoothManager.openGattServer(context, serverCallback)
    }

    fun stop(): Completable = Completable.fromAction {
        if (server == null) {
            throw Error.ServerNotOpenException
        }

        advertiser.stop().blockingAwait()
        server?.close()
        server = null
    }

    fun status(): Observable<RxBleGattServerStatus> = statusRelay

    fun devices(): Observable<List<RxBleDevice>> = deviceListRelay

    private fun handleDeviceConnected(device: BluetoothDevice) {
        if (!deviceMap.containsKey(device.address)) {
            // new device connected
            deviceMap[device.address] = RxBleDeviceImpl(device).also {
                statusRelay.accept(RxBleGattServerStatus.Disconnected(it))
            }
        }

        deviceListRelay.accept(deviceMap.values.toList())
    }

    private fun handleDeviceDisconnected(device: BluetoothDevice) {
        if (deviceMap.containsKey(device.address)) {
            deviceMap.remove(device.address)?.let {
                statusRelay.accept(RxBleGattServerStatus.Connected(it))
            }
        }

        deviceListRelay.accept(deviceMap.values.toList())
    }

    /**
     * @return true if the device supports peripheral mode
     */
    private fun isPeripheralModeSupported(bluetoothAdapter: BluetoothAdapter): Boolean {
        return bluetoothAdapter.isMultipleAdvertisementSupported
    }

    /**
     * Utility method to inject log-tag into the log message
     */
    private fun log(msg: String) = Logger.verbose(TAG, msg)

    companion object {
        const val TAG = "RxBleGattServer"
    }

    sealed class Error : Throwable() {
        object ServerAlreadyOpenException : Error()
        object ServerNotOpenException : Error()
        object BluetoothDisabledException : Error()
        object NotSupportedException : Error()
    }
}
