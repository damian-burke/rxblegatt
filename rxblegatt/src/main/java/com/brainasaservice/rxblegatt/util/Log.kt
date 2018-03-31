package com.brainasaservice.rxblegatt.util

import android.util.Log

object Logger {
    var enabled: Boolean = true

    fun verbose(tag: String, msg: String) = if (enabled) {
        Log.v(tag, msg)
    } else {
        -1
    }
}
