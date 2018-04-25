# RxBleGatt

Reactive Bluetooth Low Energy peripheral mode on Android.

## Introduction

Handling Bluetooth Low Energy on Android involves working with a lot of
callbacks. Starting from connection state changes to write requests and
read requests, everything is a callback with a reference back to it's
original source (for example the Descriptor that the client is trying
to read).

This library is an approach at stepping away from callbacks towards a
Reactive approach. Wrappers around Android's default BLE components
encapsule their behavior and manage their state, while exposing it via
`Observables`.

Starting with `Observables` monitoring the connected clients, to clients
trying to write onto `Descriptors` or `Characteristics`, all of this can
easily be handled by utilizing `RxJava`.

## Usage

The BLE stack for peripheral devices contains certain layers that need
to be available. On the bottom, there's the `Server`. Each `Server` can
contain multiple `Services`, which can be discovered by possible
clients. `Services` can host certain `Characteristics`, which are the
`I/O channel` the GATT service offers. `Characteristics` have certain
properties and permissions, allowing clients to either read, write or
both from and onto them. `Characteristics` can contain `Descriptors`,
which should - as their name suggests - describe the data the
`Characteristic` might contain.

### Server

Creating the server is straight-forward. All it requires, is the
application's `Context`. The application also needs to obtain the
`Bluetooth` permission in its Manifest.

```
val server = RxBleGattServer(context)
```

### Service

```
val service = server.addService(serviceUuid, RxBleService.Type.PRIMARY) {
    /**
    * Configure service
    */
    addCharacteristics(...) { ... }
}
```

### Advertising

```
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
}.start()
```

### Characteristic

```
    val service = server.addService(UUID.randomUUID(), RxBleService.Type.PRIMARY) {
        addCharacteristic {
            setUuid(UUID.randomUUID())
            setProperties(BluetoothGattCharacteristic.PROPERTY_BROADCAST)
            setPermissions(BluetoothGattCharacteristic.PERMISSION_WRITE)

            // add notification descriptor to characteristic
            enableNotificationSubscription()
        }
    }
```

### Descriptor

```
    val service = server.addService(UUID.randomUUID(), RxBleService.Type.PRIMARY) {
        addCharacteristic {
            ...
            addDescriptor {
                setUuid(UUID.randomUUID())
                setPermissions(BluetoothGattDescriptor.PERMISSION_READ)
            } []()
        }
    }
```

## Examples

Connecting the dots in a `reactive` way.

#### Starting the server and advertising

```
    val serverDisposable = server.start()
            .andThen(advertising)
            .subscribe({
                println("Server online and advertising initialized!")
            }, {
                println("Something went wrong: $it")
            })
```

#### Waiting for a client to write onto a Characteristic

We're observing the client with the address `1.2.3.4`

The client wants to write information onto our `characteristic` with the
`UUID == UUID.randomUUID()`.

Each `write request` the client executes, we'll send a `response`
mirroring the `write request` with a `success status code`.

Afterwards, we'll parse the received `ByteArray` with an instance of
our `GogoParser`, which contains a byte buffer in case the client's
`mtu` was too low to receive the complete message at once.

Afterwards, we'll subscribe to the emitted parsed messages.

```
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
```

### Observing Write requests onto a Characteristic

```
char.observeWriteRequests()
        .respondIfRequired {
            RxBleResponse(it.device, it.requestId, BluetoothGatt.GATT_SUCCESS, it.offset, it.value)
        }
        .parseWith(GogoParser())
        .subscribe({ message ->
        }, {
        })
```

## Download



## Contributing



## Maintainers

- [Damian Burke](https://github.com/damian-burke)

## License

This software is released under the
[Apache License v2](https://www.apache.org/licenses/LICENSE-2.0).