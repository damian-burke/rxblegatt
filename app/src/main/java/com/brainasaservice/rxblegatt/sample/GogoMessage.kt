package com.brainasaservice.rxblegatt.sample

import com.brainasaservice.rxblegatt.message.RxBleData

data class GogoMessage(
        val sync: Int,
        val length: Int,
        val data: String
) : RxBleData()
