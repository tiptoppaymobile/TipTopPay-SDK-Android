package inc.tiptoppay.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AltpayPayTransaction(
	@SerializedName("TransactionId") val transactionId: Long,
	@SerializedName("Amount") val amount: Double,
	@SerializedName("ExtensionData") val extensionData: ExtensionData?) : Parcelable

@Parcelize
data class ExtensionData(
	@SerializedName("Link") val link: String?,
	@SerializedName("Clabe") val clabe: String?,
	@SerializedName("ExpiredDate") val expiredDate: String?) : Parcelable