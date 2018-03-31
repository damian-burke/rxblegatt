package com.brainasaservice.rxblegatt

import android.bluetooth.*
import android.content.Context
import com.brainasaservice.rxblegatt.util.Logger
import io.reactivex.Completable

class RxBleGattServer(val context: Context) : BluetoothGattServerCallback() {
    private var server: BluetoothGattServer? = null

    private var bluetoothManager: BluetoothManager? = null

    private var bluetoothAdapter: BluetoothAdapter? = null

    fun start(): Completable = Completable.fromAction {
        if (server != null) {
            throw Error.ServerAlreadyOpenException
        }

        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        if (bluetoothAdapter == null || bluetoothAdapter?.isEnabled == false) {
            throw Error.BluetoothDisabledException
        }

        bluetoothAdapter?.let {
            if (!isPeripheralModeSupported(it)) {
                throw Error.NotSupportedException
            }
        }

        server = bluetoothManager?.openGattServer(context, this)
    }

    fun stop(): Completable = Completable.fromAction {
        if (server == null) {
            throw Error.ServerNotOpenException
        }

        /**
         * TODO: stop advertising, clear devices
         */
        server?.close()
        server = null
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        log("onConnectionStateChange(device=$device, status=$status, newState=$newState)")
        /**
         * TODO: manage device observables and lists
         */
    }

    /**
     * TODO: Add additional checks (extended advertising, periodic advertising)
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
