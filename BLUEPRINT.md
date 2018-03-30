## Blueprint - How to use it

### Server
* Observe
  * Open
    * Throw exception if BT is disabled
    * Requires context
  * Close
  * Status
  * Connected devices
* Set
  * Default MTU
* ServerStatus [OPEN, CONNECTED(device), DISCONNECTED(device), CLOSED, ERROR(reason)]


### Services
* Add services with UUID + SERVICE_TYPE
* Add characteristics to service
* Add descriptors to characteristics
* onServiceAdded(status, service) -> callback for successfull initializing
* ServiceStatus [ADDED, REMOVED, ERROR(reason)]

### Advertising
* Settings
* Data
* Response data
* AdvertiseStatus [ACTIVE, ERROR(reason), INACTIVE]

### Characteristics
* Write new value to characteristic
  * Respect the MTU! 
* Notify single device / all devices about characteristic changes
  * Optional: Wait for notification to be sent
* onCharacteristicWriteRequest (client requests to write data to us)
* onCharacteristicReadRequest (client requests to read data from us)
* enable notification subscription (adds descriptor)

#### Descriptors

* onDescriptorWriteRequest (client requests to write data to a descriptor)
* onDescriptorReadRequest (client wants to read data from descriptor)


    val server = RxBleGattServer.with(context)
    
    val service = RxBleService(UUID, SERVICE_TYPE_PRIMARY)
    
    val characteristic = RxBleCharacteristic()
    
    service.addCharacteristic(characteristic)
    
    server.addService(service)
    
    server.start().subscribe({ 
        - OPEN
        - CONNECTED(1.22.10.2)
        - DISCONNECTED(1.22.10.2)
        - ERROR (random_error)
        - CLOSED
    }, { e -> 
        println("server crashed with $e")
    })

    server.advertiser
        .setup(data, response, settings)
        .start()
    
    server.advertiser.observe() // keep an eye on the status

    characteristic.observeWriteRequest() -> Observable<RxBleCharacteristicWriteRequest>
        .flatMap { characteristicWriteRequest -> 
            
            server.respond(it.response(
        }
    
    RxBleCharacteristicWriteRequest {
        characteristic: RxBleCharacteristic,
        device: RxBluetoothDevice,
        // etc... (requestId, RxBleDescriptor, preparedWrite, responseNeeded, offset, value)
        
        fun response(): RxBleResponse {
            RxBleDevice,
            requestId,
            status,
            offset,
            value
        }
    }