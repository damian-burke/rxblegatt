package com.brainasaservice.rxblegatt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import com.brainasaservice.rxblegatt.advertiser.RxBleAdvertiser
import com.brainasaservice.rxblegatt.advertiser.RxBleAdvertiserImpl
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicReadRequest
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicWriteRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorReadRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorWriteRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleNotificationDescriptor
import com.brainasaservice.rxblegatt.device.RxBleDevice
import com.brainasaservice.rxblegatt.device.RxBleDeviceImpl
import com.brainasaservice.rxblegatt.service.RxBleService
import com.brainasaservice.rxblegatt.service.RxBleServiceImpl
import com.brainasaservice.rxblegatt.util.Logger
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.Completable
import io.reactivex.Observable
import java.util.UUID

class RxBleGattServer(private val context: Context) {
    val advertiser: RxBleAdvertiser by lazy {
        RxBleAdvertiserImpl(bluetoothAdapter)
    }

    private var server: BluetoothGattServer? = null

    private val deviceMap: HashMap<String, RxBleDevice> = hashMapOf()

    private val serviceMap: HashMap<UUID, RxBleService> = hashMapOf()

    private val deviceSubject: ReplaySubject<RxBleDevice> = ReplaySubject.create(DEVICE_BUFFER)

    private val statusSubject: PublishSubject<RxBleGattServerStatus> = PublishSubject.create()

    private val deviceListSubject: BehaviorSubject<List<RxBleDevice>> = BehaviorSubject.create()

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
            service?.uuid?.let { uuid ->
                serviceMap[uuid]?.onServiceAdded()
            }
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
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
                    val request = RxBleDescriptorReadRequest(this@RxBleGattServer, rxDevice, rxDescriptor, requestId, offset)
                    rxDescriptor.onReadRequest(request)
                    rxDevice.onDescriptorReadRequest(request)
                }
            }
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
                         * If the current descriptor is a Notification descriptor, set subscription
                         * TODO: update the descriptor / characteristic with subscription?
                         */
                        if (value?.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE) == true) {
                            rxDevice.notificationSubscriptionInactive(rxCharacteristic)
                        } else if (value?.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == true) {
                            rxDevice.notificationSubscriptionActive(rxCharacteristic)
                        }
                    } else {
                        /**
                         * Otherwise, forward the write request to the RxBleDescriptor
                         */
                        val request = RxBleDescriptorWriteRequest(rxDevice, rxDescriptor, requestId, preparedWrite, responseNeeded, offset, value)
                        rxDescriptor.onWriteRequest(request)
                        rxDevice.onDescriptorWriteRequest(request)
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

        override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            /**
             * TODO: update RxBleCharacteristic
             */
            characteristic?.let {
                val serviceUuid = it.service.uuid
                val charUuid = it.uuid

                val rxCharacteristic = serviceMap[serviceUuid]?.characteristicMap?.get(charUuid)
                val rxDevice = deviceMap[device?.address]

                if (rxCharacteristic != null && rxDevice != null) {
                    val request = RxBleCharacteristicReadRequest(
                            this@RxBleGattServer,
                            rxDevice,
                            rxCharacteristic,
                            requestId,
                            offset
                    )
                    rxCharacteristic.onReadRequest(request)
                    rxDevice.onCharacteristicReadRequest(request)
                }
            }
        }

        override fun onCharacteristicWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            characteristic?.let {
                val serviceUuid = it.service.uuid
                val charUuid = it.uuid

                val rxCharacteristic = serviceMap[serviceUuid]?.characteristicMap?.get(charUuid)
                val rxDevice = deviceMap[device?.address]

                if (rxCharacteristic != null && rxDevice != null) {
                    val request = RxBleCharacteristicWriteRequest(
                            this@RxBleGattServer,
                            rxDevice,
                            rxCharacteristic,
                            requestId,
                            preparedWrite,
                            responseNeeded,
                            offset,
                            value
                    )
                    rxCharacteristic.onWriteRequest(request)
                    rxDevice.onCharacteristicWriteRequest(request)
                }
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            device?.let {
                deviceMap[it.address]?.onNotificationSent()
            }
        }
    }

    fun sendResponse(response: RxBleResponse): Completable = Completable.fromAction {
        if (server == null) {
            throw Error.ServerNotOpenException
        }

        val result = server?.sendResponse(
                response.device.device,
                response.requestId,
                response.status,
                response.offset,
                response.value
        ) ?: false

        if (!result) {
            throw Error.ResponseNotSentException
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

    fun status(): Observable<RxBleGattServerStatus> = statusSubject

    fun devices(): Observable<RxBleDevice> = deviceSubject

    fun deviceList(): Observable<List<RxBleDevice>> = deviceListSubject

    fun addService(uuid: UUID, type: RxBleService.Type): RxBleService {
        val service = RxBleServiceImpl(uuid, type)
        serviceMap[uuid] = service
        server?.addService(service.service)
        return service
    }

    private fun handleDeviceConnected(device: BluetoothDevice) {
        if (!deviceMap.containsKey(device.address)) {
            // new device connected, add to map + push in devices relay
            deviceMap[device.address] = RxBleDeviceImpl(device).also {
                statusSubject.onNext(RxBleGattServerStatus.Connected(it))
                deviceSubject.onNext(it)
            }
        } else {
            deviceMap[device.address]?.setConnected()
        }

        deviceListSubject.onNext(deviceMap.values.toList())
    }

    private fun handleDeviceDisconnected(device: BluetoothDevice) {
        if (deviceMap.containsKey(device.address)) {
            deviceMap[device.address]?.let {
                statusSubject.onNext(RxBleGattServerStatus.Disconnected(it))
                it.setDisconnected()
            }
        }

        deviceListSubject.onNext(deviceMap.values.toList())
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

        const val DEVICE_BUFFER = 24
    }

    sealed class Error : Throwable() {
        object ServerAlreadyOpenException : Error()
        object ServerNotOpenException : Error()
        object BluetoothDisabledException : Error()
        object NotSupportedException : Error()
        object ResponseNotSentException : Error()
    }
}
