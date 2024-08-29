package inc.tiptoppay.sdk.api.models

import com.google.gson.annotations.SerializedName

data class TipTopPayBinInfoResponse(
		@SerializedName("Success") val success: Boolean?,
		@SerializedName("Message") val message: String?,
		@SerializedName("Model") val binInfo: TipTopPayBinInfo?)

data class TipTopPayBinInfo(
		@SerializedName("LogoUrl") val logoUrl: String?,
		@SerializedName("BankName") val bankName: String?)