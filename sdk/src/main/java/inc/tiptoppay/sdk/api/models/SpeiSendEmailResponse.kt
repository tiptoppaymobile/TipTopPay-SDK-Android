package inc.tiptoppay.sdk.api.models

import com.google.gson.annotations.SerializedName

data class SpeiSendEmailResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("ErrorCode") val errorCode: String?)
