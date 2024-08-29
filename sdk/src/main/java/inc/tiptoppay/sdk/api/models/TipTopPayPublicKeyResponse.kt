package inc.tiptoppay.sdk.api.models

import com.google.gson.annotations.SerializedName
data class TipTopPayPublicKeyResponse(
		@SerializedName("Pem") val pem: String?,
		@SerializedName("Version") val version: Int?)

