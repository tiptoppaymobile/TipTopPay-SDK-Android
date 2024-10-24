package inc.tiptoppay.sdk.models

import inc.tiptoppay.sdk.api.models.InstallmentsVariant

data class SDKConfiguration(
	var publicKey: PublicKey = PublicKey(pem = null, version = null),
	var availablePaymentMethods: AvailablePaymentMethods = AvailablePaymentMethods(),
	var terminalConfiguration: TerminalConfiguration = TerminalConfiguration(),
	var installmentsVariant: ArrayList<InstallmentsVariant> = ArrayList(),
	var cashMinAmount: Int = 0,
	var cashMethods: ArrayList<Int> = ArrayList(),
	var saveCard: Boolean? = null
	)

data class PublicKey(
	var pem: String? = null,
	var version: Int? = null
)

data class AvailablePaymentMethods(
	var googlePayAvailable: Boolean = false,
	var installmentsAvailable: Boolean = false,
	var cashAvailable: Boolean = false
)

data class TerminalConfiguration(
	var gPayGatewayName: String = "",
	var isSaveCard: Int? = null,
	var skipExpiryValidation: Boolean? = null
)
