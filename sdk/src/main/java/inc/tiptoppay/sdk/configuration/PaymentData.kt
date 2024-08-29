package inc.tiptoppay.sdk.configuration

import android.os.Parcelable
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import inc.tiptoppay.sdk.api.models.PaymentDataPayer
import inc.tiptoppay.sdk.util.TAG

@Parcelize
class PaymentData(
	val amount: String,
	var currency: String = "RUB",
	val invoiceId: String? = null,
	val description: String? = null,
	val accountId: String? = null,
	var email: String? = null,
	val payer: PaymentDataPayer? = null,
	val jsonData: String? = null
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
