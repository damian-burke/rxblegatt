package com.brainasaservice.rxblegatt.advertiser

import io.reactivex.Completable

interface RxBleAdvertiser {
    fun start(): Completable

    fun stop(): Completable
}
