package com.brainasaservice.rxblegatt.device

import android.bluetooth.BluetoothDevice
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristic
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicReadRequest
import com.brainasaservice.rxblegatt.characteristic.RxBleCharacteristicWriteRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorReadRequest
import com.brainasaservice.rxblegatt.descriptor.RxBleDescriptorWriteRequest
import io.reactivex.Observable

interface RxBleDevice {
    val device: BluetoothDevice

    fun observeConnection(): Observable<RxBleDevice.Connection>

    fun observeStatus(): Observable<RxBleDevice.Status>

    fun observeCharacteristicWriteRequests(): Observable<RxBleCharacteristicWriteRequest>

    fun observeCharacteristicReadRequests(): Observable<RxBleCharacteristicReadRequest>

    fun observeDescriptorWriteRequests(): Observable<RxBleDescriptorWriteRequest>

    fun observeDescriptorReadRequests(): Observable<RxBleDescriptorReadRequest>

    fun setMtu(mtu: Int)

    fun notificationSubscriptionActive(characteristic: RxBleCharacteristic)

    fun notificationSubscriptionInactive(characteristic: RxBleCharacteristic)

    fun onCharacteristicWriteRequest(request: RxBleCharacteristicWriteRequest)

    fun onCharacteristicReadRequest(request: RxBleCharacteristicReadRequest)

    fun onDescriptorReadRequest(request: RxBleDescriptorReadRequest)

    fun onDescriptorWriteRequest(request: RxBleDescriptorWriteRequest)

    fun onNotificationSent()

    fun isConnected(): Boolean

    /**
     * To be called if the device connected / reconnected  to the server.
     */
    fun setConnected()

    /**
     * To be called if the device disconnected from the server.
     */
    fun setDisconnected()

    sealed class Status {
        data class OnMtuChanged(val mtu: Int) : Status()
        data class OnNotificationSubscriptionActive(val characteristic: RxBleCharacteristic) : Status()
        data class OnNotificationSubscriptionInactive(val characteristic: RxBleCharacteristic) : Status()
        object OnNotificationSent : Status()
    }

    enum class Connection {
        CONNECTED,
        DISCONNECTED
    }
}
