package inc.tiptoppay.sdk.log

import android.app.Activity
import android.util.Log

class TipTopPayUncaughtExceptionHandler private constructor(val activity: Activity) : Thread.UncaughtExceptionHandler {

    companion object {

        @Volatile private var INSTANCE: TipTopPayUncaughtExceptionHandler? = null

        fun getInstance(activity: Activity): TipTopPayUncaughtExceptionHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TipTopPayUncaughtExceptionHandler(activity).also {
                    INSTANCE = it
                }
            }
        }
    }

    override fun uncaughtException(thread: Thread, e: Throwable) {

        val message = e.message.toString()
        val stackTrace = e.stackTraceToString()

        Log.e("TTP_SDK_ERROR_MESSAGE", message)
        Log.e("TTP_SDK_ERROR_STACK_TRACE", stackTrace)

        TipTopPaySendLogHttpClient.sendErrorLog(message, stackTrace)
        Thread.sleep(4000)

        activity.finish()
    }
}