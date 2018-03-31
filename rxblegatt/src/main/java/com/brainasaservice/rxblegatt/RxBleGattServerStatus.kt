package com.brainasaservice.rxblegatt

import com.brainasaservice.rxblegatt.device.RxBleDevice

sealed class RxBleGattServerStatus {
    /**
     * Server has been opened successfully.
     */
    object Open : RxBleGattServerStatus()

    /**
     * Server has been shut down gracefully.
     */
    object Closed: RxBleGattServerStatus()

    /**
     * A new device connected.
     */
    data class Connected(val device: RxBleDevice): RxBleGattServerStatus()

    /**
     * A device has disconnected.
     */
    data class Disconnected(val device: RxBleDevice): RxBleGattServerStatus()
}
