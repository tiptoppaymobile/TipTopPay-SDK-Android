package inc.tiptoppay.sdk.api.models

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable

data class AltpayPayTransactionResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("ErrorCode") val errorCode: String?,
	@SerializedName("Model") val transaction: AltpayPayTransaction?) {
	fun handleError(): Observable<AltpayPayTransactionResponse> {
		return if (success == true){
			Observable.just(this)
		} else {
			Observable.error(TipTopPayTransactionError(message.orEmpty()))
		}
	}
}