package inc.tiptoppay.sdk.api.models

import com.google.gson.annotations.SerializedName

data class SpeiSendEmailBody(
	@SerializedName("transactionId") val transactionId: Long,
	@SerializedName("email") val email: String)