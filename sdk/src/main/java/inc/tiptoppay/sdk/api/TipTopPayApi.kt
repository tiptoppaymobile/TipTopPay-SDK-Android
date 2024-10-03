package inc.tiptoppay.sdk.api

import android.net.Uri
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import inc.tiptoppay.sdk.api.models.*
import java.net.URLDecoder
import javax.inject.Inject

class TipTopPayApi @Inject constructor(private val apiService: TipTopPayApiService) {
	companion object {
		private const val THREE_DS_SUCCESS_URL = "https://api.tiptoppay.kz/threeds/success"
		private const val THREE_DS_FAIL_URL = "https://api.tiptoppay.kz/threeds/fail"
	}

	fun getPublicKey(): Single<TipTopPayPublicKeyResponse> {
		return apiService.getPublicKey()
			.subscribeOn(Schedulers.io())
	}

	fun getMerchantConfiguration(publicId: String): Single<TipTopPayMerchantConfigurationResponse> {
		return apiService.getMerchantConfiguration(publicId)
			.subscribeOn(Schedulers.io())
	}

	fun getInstallmentsConfiguration(publicId: String, amount: String): Single<TipTopPayInstallmentsConfigurationResponse> {
		return apiService.getInstallmentsConfiguration(publicId, amount)
			.subscribeOn(Schedulers.io())
	}

	fun charge(requestBody: PaymentRequestBody): Single<TipTopPayTransactionResponse> {
		return apiService.charge(requestBody)
			.subscribeOn(Schedulers.io())
	}

	fun auth(requestBody: PaymentRequestBody): Single<TipTopPayTransactionResponse> {
		return apiService.auth(requestBody)
			.subscribeOn(Schedulers.io())
	}

	fun postThreeDs(transactionId: String, threeDsCallbackId: String, paRes: String): Single<TipTopPayThreeDsResponse> {
		val md = ThreeDsMdData(transactionId = transactionId, threeDsCallbackId = threeDsCallbackId, successURL = THREE_DS_SUCCESS_URL, failURL = THREE_DS_FAIL_URL)
		val mdString = Gson().toJson(md)
		return apiService.postThreeDs(ThreeDsRequestBody(md = mdString, paRes = paRes))
			.subscribeOn(Schedulers.io())
			.map { TipTopPayThreeDsResponse(true, "", 0) }
			.onErrorReturn {
				val response: TipTopPayThreeDsResponse = if (it is HttpException && it.response()?.raw()!!.isRedirect) {
					val url = it.response()?.raw()?.header("Location")
					when {
						url?.startsWith(THREE_DS_FAIL_URL) == true -> {
							val uri = Uri.parse(url)
							val cardholderMessage = uri.getQueryParameter("CardHolderMessage")
							val reasonCode = uri.getQueryParameter("ReasonCode")?.toIntOrNull()
							val message = if (cardholderMessage != null) URLDecoder.decode(cardholderMessage, "utf-8") else ""
							TipTopPayThreeDsResponse(false, message, reasonCode)
						}
						url?.startsWith(THREE_DS_SUCCESS_URL) == true -> TipTopPayThreeDsResponse(true, null, 0)
						else -> TipTopPayThreeDsResponse(false, null, null)
					}
				} else {
					TipTopPayThreeDsResponse(true, null, 0)
				}

				response
			}
	}

	fun getBinInfo(firstSixDigits: String): Single<TipTopPayBinInfo> =
		if (firstSixDigits.length < 6) {
			Single.error(TipTopPayTransactionError("You must specify the first 6 digits of the card number"))
		} else {
			val firstSix = firstSixDigits.subSequence(0, 6).toString()
			apiService.getBinInfo(firstSix)
					.subscribeOn(Schedulers.io())
					.map { it.binInfo ?: TipTopPayBinInfo("", "") }
					.onErrorReturn { TipTopPayBinInfo("", "") }
		}

	fun getBinInfo(queryMap: Map<String, String>): Single<TipTopPayBinInfoResponse> {
		return apiService.getBinInfo(queryMap)
			.subscribeOn(Schedulers.io())
	}

}