package com.brainasaservice.rxblegatt.device

import com.brainasaservice.rxblegatt.parser.RxBleParser
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * If a disconnected event is propagated in this device's connection Observable, the given
 * RxBleParser instance will be instructed to clear the device's byte buffer to allow for
 * a clean reconnection.
 *
 * @param parser RxBleParser instance used to parse the device's messages.
 */
fun Observable<RxBleDevice>.clearBufferOnDisconnect(parser: RxBleParser): Observable<RxBleDevice> {
    return this.flatMap { device ->
        device.observeConnection()
                .flatMapCompletable {
                    if (it == RxBleDevice.Connection.DISCONNECTED) {
                        Completable.fromAction {
                            parser.clear(device)
                        }
                    } else {
                        Completable.complete()
                    }
                }
                .andThen(Observable.just(device))
    }
}