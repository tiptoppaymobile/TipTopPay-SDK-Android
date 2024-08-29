package inc.tiptoppay.sdk.models

import inc.tiptoppay.sdk.configuration.TipTopPaySDK

data class Transaction (
    val transactionId: Long?,
    val status: TipTopPaySDK.TransactionStatus?,
    val reasonCode: Int?
	)