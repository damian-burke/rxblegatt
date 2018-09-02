package com.brainasaservice.rxblegatt.sample

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import com.brainasaservice.rxblegatt.RxBleGattServer
import com.brainasaservice.rxblegatt.RxBleResponse
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicWriteRequest
import com.brainasaservice.rxblegatt.characteristic.parseWith
import com.brainasaservice.rxblegatt.characteristic.respondIfRequired
import com.brainasaservice.rxblegatt.device.RxBleDevice
import com.brainasaservice.rxblegatt.device.clearBufferOnDisconnect
import com.brainasaservice.rxblegatt.service.RxBleService
import io.reactivex.Observable
import java.util.UUID

/**
 * Test-file to test how certain aspects of the syntax look and feel.
 */

fun x(context: Context) {
    /**
     * Initializing the server with a context.
     */
    val server = RxBleGattServer(context)

    /**
     * Preparing advertising data (doesn't support multiple advertising data so far)
     */
    val advertising = server.advertiser.apply {
        data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build()

        settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build()
    }
            .start()

    // start server + start advertising
    val serverDisposable = server.start()
            .andThen(advertising)
            .subscribe({
                println("Server online and advertising initialized!")
            }, {
                println("Something went wrong: $it")
            })

    /**
     * Subscribe to the server-observeStatus
     */
    val serverStatusDisposable = server.status()
            .subscribe({
                println("Status: $it")
            }, {
                println("Something went wrong... $it")
            })

    /**
     * Subscribe to connected devices as list
     */
    val deviceDisposable = server.deviceList()
            .subscribe({
                println("We have ${it.size} devices")
            }, {
                println("Something went wrong... $it")
            })

    /**
     * Subscribe to flattened device observable
     */
    val device = server.devices()
            .filter { it.device.address == "1.2.3.4" }
            .onCharacteristicWriteRequest { it.characteristic.uuid == UUID.randomUUID() }
            .respondIfRequired {
                RxBleResponse(it.device, it.requestId, BluetoothGatt.GATT_SUCCESS, it.offset, it.value)
            }
            .parseWith(GogoParser())
            .subscribe({ message ->

            }, { error ->
                println("Something went wrong...")
            })

    /**
     * Add a service to the server.
     */
    var char: RxBleCharacteristic? = null

    val service = server.addService(UUID.randomUUID(), RxBleService.Type.PRIMARY) {
        addCharacteristic {
            setUuid(UUID.randomUUID())
            setProperties(BluetoothGattCharacteristic.PROPERTY_BROADCAST)
            setPermissions(BluetoothGattCharacteristic.PERMISSION_WRITE)

            addDescriptor {
                setUuid(UUID.randomUUID())
                setPermissions(BluetoothGattDescriptor.PERMISSION_READ)
            }

            enableNotificationSubscription()
        }.also { char = it }
    }

    char!!.observeWriteRequests()
            .respondIfRequired {
                RxBleResponse(it.device, it.requestId, BluetoothGatt.GATT_SUCCESS, it.offset, it.value)
            }
            .parseWith(GogoParser())
            .subscribe({ message ->

            }, {

            })

    val veryGoodCharacteristicUuid = UUID.randomUUID()

    /**
     * Add characteristic to the service
     * TODO: check if this can be done after adding the service to the server.
     */
    val characteristic = service.addCharacteristic {
        setUuid(veryGoodCharacteristicUuid)
        setPermissions(BluetoothGattCharacteristic.PERMISSION_WRITE)
        setProperties(BluetoothGattCharacteristic.PROPERTY_INDICATE)
    }

    /**
     * Observe write requests on added characteristic
     */
    val characteristicDisposable = characteristic.observeWriteRequests()
            .respondIfRequired { request ->
                RxBleResponse(request.device, request.requestId, BluetoothGatt.GATT_SUCCESS, request.offset, request.value)
            }
            .parseWith(GogoParser())
            .subscribe({ request ->
                println("Received message!")
                println("Message from ${request.sender}")
                println("Message object is ${request.message}")
            }, { error -> println("??? $error") })

    /**
     * Enable notification subscriptions for characteristic.
     * This adds a descriptor with the static UUID required.
     */
    characteristic.enableNotificationSubscription()

    println("do we allow subscriptions to notifications? ${characteristic.hasNotificationSubscriptionEnabled()}")

    /**
     * Disable it.
     */
    characteristic.disableNotificationSubscription()

    /**
     * Add descriptor to characteristic and observe write requests
     */
    val descriptorDisposable = characteristic.addDescriptor {
        setUuid(UUID.randomUUID())
        setPermissions(BluetoothGattDescriptor.PERMISSION_READ)
    }
            .observeWriteRequests()
            .subscribe({ request ->
                println("??? pls write")
            })

    /**
     * Create message parser instance
     */
    val parser = GogoParser()

    /**
     * Listen to devices
     * - Filter, to only use connected ones
     * - Clear message byte buffer of each device once it has disconnected
     * - Observe Characteristic Write Requests
     * - Filter to only read messages on our favorite characteristic
     * - Send GATT response if required
     * - Parse (which adds to byte-pool, tries to parse, and emits via Observable)
     * Finally, subscribe to the parsed messages.
     */
    val messageDisposable = server.devices()
            .filter { it.isConnected() }
            .doOnNext { println("Connected device: $it") }
            .clearBufferOnDisconnect(parser)
            .flatMap { it.observeCharacteristicWriteRequests() }
            .filter { it.characteristic.uuid == veryGoodCharacteristicUuid }
            .respondIfRequired { RxBleResponse(it.device, it.requestId, BluetoothGatt.GATT_SUCCESS, it.offset, it.value) }
            .parseWith(parser)
            .subscribe({ msg ->
            }, { error ->
                println("Something went wrong... $error")
            })

    val charMessageDisposable = characteristic.observeWriteRequests()
            .respondIfRequired { RxBleResponse(it.device, it.requestId, BluetoothGatt.GATT_SUCCESS, it.offset, it.value) }
            .parseWith(parser)
            .subscribe({ msg ->
                println("Characteristic received message ${msg.message}")
            }, {
                println("Something went wrong... $it")
            })

}

/**
 * Extension function to map a device to its characteristic write requests.
 */
fun Observable<RxBleDevice>.onCharacteristicWriteRequest(): Observable<RxBleCharacteristicWriteRequest> {
    return this.flatMap {
        it.observeCharacteristicWriteRequests()
    }
}

/**
 * ^---
 * With a predicate to watch only certain characteristics.
 */
fun Observable<RxBleDevice>.onCharacteristicWriteRequest(predicate: (RxBleCharacteristicWriteRequest) -> Boolean): Observable<RxBleCharacteristicWriteRequest> {
    return this.flatMap {
        it.observeCharacteristicWriteRequests()
                .filter(predicate)
    }
}





