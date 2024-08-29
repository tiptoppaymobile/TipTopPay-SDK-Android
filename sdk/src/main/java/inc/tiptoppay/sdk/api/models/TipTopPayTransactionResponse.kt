package inc.tiptoppay.sdk.api.models

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable

data class TipTopPayTransactionResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("Model") val transaction: TipTopPayTransaction?) {
	fun handleError(): Observable<TipTopPayTransactionResponse> {
		return if (success == true || (!transaction?.acsUrl.isNullOrEmpty() && !transaction?.paReq.isNullOrEmpty())){
			Observable.just(this)
		} else {
			Observable.error(TipTopPayTransactionError(message ?: transaction?.cardHolderMessage.orEmpty()))
		}
	}
}