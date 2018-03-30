package com.brainasaservice.rxblegatt.service

import android.bluetooth.BluetoothGattService

enum class RxBleServiceType(val value: Int) {
    PRIMARY(BluetoothGattService.SERVICE_TYPE_PRIMARY),
    SECONDARY(BluetoothGattService.SERVICE_TYPE_SECONDARY)
}