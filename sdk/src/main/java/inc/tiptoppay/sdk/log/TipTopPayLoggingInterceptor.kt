package inc.tiptoppay.sdk.log

import okhttp3.Interceptor
import okhttp3.Response

class TipTopPayLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        val response = chain.proceed(request)
        val statusCode = response.code

        TipTopPaySendLogHttpClient.sendApiLog(method, url, statusCode.toString())
        return response
    }
}