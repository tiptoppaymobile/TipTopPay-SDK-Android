package inc.tiptoppay.sdk.api.models

import com.google.gson.annotations.SerializedName

data class TipTopPayInstallmentsConfigurationResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("Model") val model: InstallmentsConfiguration?
)

data class InstallmentsConfiguration(
	@SerializedName("IsCardInstallmentAvailable") val isCardInstallmentAvailable: Boolean?,
	@SerializedName("Configuration") val configuration: ArrayList<InstallmentsVariant>?
)

data class InstallmentsVariant(
	@SerializedName("Term") val term: Int,
	@SerializedName("MonthlyPayment") val monthlyPayment: Double
)