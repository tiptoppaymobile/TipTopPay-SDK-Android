package inc.tiptoppay.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TipTopPayTransaction(
	@SerializedName("TransactionId") val transactionId: Long?,
	@SerializedName("ReasonCode") val reasonCode: Int?,
	@SerializedName("CardHolderMessage") val cardHolderMessage: String?,
	@SerializedName("PaReq") val paReq: String?,
	@SerializedName("AcsUrl") val acsUrl: String?,
	@SerializedName("ThreeDsCallbackId") val threeDsCallbackId: String?) : Parcelable