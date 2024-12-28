package inc.tiptoppay.sdk.configuration

import android.os.Parcelable
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import inc.tiptoppay.sdk.api.models.PaymentDataPayer
import inc.tiptoppay.sdk.api.models.PaymentDataReceipt
import inc.tiptoppay.sdk.api.models.PaymentDataRecurrent
import inc.tiptoppay.sdk.models.Currency
import inc.tiptoppay.sdk.util.TAG
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class PaymentData(
	val amount: String,
	var currency: Currency,
	val invoiceId: String? = null,
	val description: String? = null,
	val accountId: String? = null,
	var email: String? = null,
	val payer: PaymentDataPayer? = null,
	val recurrent: PaymentDataRecurrent? = null,
	val receipt: PaymentDataReceipt? = null,
	private val jsonData: String? = null
) : Parcelable {

	fun jsonDataHasRecurrent(): Boolean {

		if (!jsonData.isNullOrEmpty()) {
			val gson = GsonBuilder()
				.setLenient()
				.create()

			try {
				val ttpJsonData = gson.fromJson(jsonData, TtpJsonData::class.java)
				ttpJsonData.payJsonData?.recurrent?.interval?.let {
					return true
				}
			} catch (e: JsonSyntaxException) {
				Log.e(TAG, "JsonData syntax error")
			}
		}
		return false
	}

	fun getJsonData(): String? {
		var baseData = mutableMapOf<String, Any>()

		val existingJsonData = this.jsonData
		if (!existingJsonData.isNullOrEmpty()) {
			convertStringToDictionary(existingJsonData)?.let {
				baseData = it.toMutableMap()
			}
		}

		val tipTopPay = (baseData["PaymentData"] as? Map<String, Any>)?.toMutableMap() ?: mutableMapOf()

		recurrent?.let {
			tipTopPay["recurrent"] = recurrent
		}

		receipt?.let {
			tipTopPay["CustomerReceipt"] = receipt
		}

		if (tipTopPay.isNotEmpty()) {
			baseData["PaymentData"] = tipTopPay
		}

		return try {
			val encoder = Gson()
			val jsonData = encoder.toJsonTree(baseData).asJsonObject.toString()
			jsonData
		} catch (e: JSONException) {
			println("Failed to serialize JSON: ${e.localizedMessage}")
			null
		}
	}

	fun convertStringToDictionary(jsonString: String): Map<String, Any>? {
		return try {
			jsonObjectToMap(JSONObject(jsonString))
		} catch (e: JSONException) {
			println("Failed to parse JSON string to Map: ${e.localizedMessage}")
			null
		}
	}

	fun jsonObjectToMap(jObject: JSONObject): Map<String, Any> {
		val map = mutableMapOf<String, Any>()
		val keys = jObject.keys()
		while (keys.hasNext()) {
			val key = keys.next()
			map[key] = jObject.get(key)
		}
		return map
	}
}

data class TtpJsonData(
	@SerializedName("PaymentData") val payJsonData: TipTopPayJsonData?
)

data class TipTopPayJsonData(
	@SerializedName("recurrent") val recurrent: TipTopPayRecurrentJsonData?
)

data class TipTopPayRecurrentJsonData(
	@SerializedName("interval") val interval: String?,
	@SerializedName("period") val period: String?,
	@SerializedName("amount") val amount: String?
)
