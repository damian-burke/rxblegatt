package com.brainasaservice.rxblegatt.characteristic

import com.brainasaservice.rxblegatt.RxBleResponse
import com.brainasaservice.rxblegatt.message.RxBleMessage
import com.brainasaservice.rxblegatt.parser.RxBleParser
import io.reactivex.Observable

/**
 * If the setValue request had the "response needed" flag set to true, a response with given parameters
 * will be sent to the device that originally sent the setValue request.
 *
 * @param block Function to create the RxBleResponse based on the setValue request.
 */
fun Observable<RxBleCharacteristicWriteRequest>.respondIfRequired(block: (request: RxBleCharacteristicWriteRequest) -> RxBleResponse): Observable<RxBleCharacteristicWriteRequest> {
    return this.flatMap {
        if (it.responseNeeded) {
            it.server.sendResponse(block(it)).andThen(Observable.just(it))
        } else {
            Observable.just(it)
        }
    }
}

/**
 * Given a RxBleParser instance, this method will pass incoming bytes into the parser. Once the
 * parser returns a parsed non-null data, it will be passed down-stream.
 */
fun Observable<RxBleCharacteristicWriteRequest>.parseWith(parser: RxBleParser): Observable<RxBleMessage> {
    return this.flatMap { request ->
        request.value?.let {
            parser.parse(request.device, it).map {
                RxBleMessage(
                        it,
                        request.device
                )
            }
        } ?: Observable.never()
    }
}
