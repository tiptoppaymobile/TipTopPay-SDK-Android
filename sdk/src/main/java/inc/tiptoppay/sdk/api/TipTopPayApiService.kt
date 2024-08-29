package inc.tiptoppay.sdk.api

import inc.tiptoppay.sdk.api.models.TipTopPayBinInfoResponse
import inc.tiptoppay.sdk.api.models.TipTopPayMerchantConfigurationResponse
import inc.tiptoppay.sdk.api.models.TipTopPayPublicKeyResponse
import inc.tiptoppay.sdk.api.models.TipTopPayTransactionResponse
import inc.tiptoppay.sdk.api.models.PaymentRequestBody
import inc.tiptoppay.sdk.api.models.ThreeDsRequestBody
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TipTopPayApiService {
	@POST("payments/cards/charge")
	fun charge(@Body body: PaymentRequestBody): Single<TipTopPayTransactionResponse>

	@POST("payments/cards/auth")
	fun auth(@Body body: PaymentRequestBody): Single<TipTopPayTransactionResponse>

	@POST("payments/ThreeDSCallback")
	fun postThreeDs(@Body body: ThreeDsRequestBody): Single<Boolean>

	@GET("bins/info/{firstSixDigits}")
	fun getBinInfo(@Path("firstSixDigits") firstSixDigits: String): Single<TipTopPayBinInfoResponse>

	@GET("payments/publickey")
	fun getPublicKey(): Single<TipTopPayPublicKeyResponse>

	@GET("merchant/configuration")
	fun getMerchantConfiguration(@Query("terminalPublicId") publicId: String): Single<TipTopPayMerchantConfigurationResponse>
}