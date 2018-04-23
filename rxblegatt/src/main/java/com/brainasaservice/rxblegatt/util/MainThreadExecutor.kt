package com.brainasaservice.rxblegatt.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class MainThreadExecutor() : Executor {
    private final val handler: Handler = Handler(Looper.getMainLooper());

    override fun execute(runnable: Runnable?) {
        runnable?.apply { handler.post(this) }
    }
}
