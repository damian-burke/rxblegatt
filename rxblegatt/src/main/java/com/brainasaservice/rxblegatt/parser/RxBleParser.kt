package com.brainasaservice.rxblegatt.parser

import com.brainasaservice.rxblegatt.message.RxBleData
import io.reactivex.Observable

/**
 * TODO: byte-pool on a per-device basis to allow multiple devices sending data simultaneously.
 */
abstract class RxBleParser {
    private var bytePool: ByteArray = ByteArray(0)

    fun parse(bytes: ByteArray): Observable<RxBleData> {
        return read(bytePool)?.let { Observable.just(it) } ?: Observable.never()
    }

    fun clear() {
        bytePool = ByteArray(0)
    }

    fun drop(byteCount: Int = 1) {
        bytePool = bytePool.drop(byteCount).toByteArray()
    }

    abstract fun read(bytes: ByteArray): RxBleData?
}
