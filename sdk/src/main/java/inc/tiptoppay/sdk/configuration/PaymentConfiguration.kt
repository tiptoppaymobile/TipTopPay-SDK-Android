package inc.tiptoppay.sdk.configuration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import inc.tiptoppay.sdk.scanner.CardScanner

@Parcelize
data class PaymentConfiguration(val publicId: String,
								val paymentData: PaymentData,
								val scanner: CardScanner?,
								val requireEmail: Boolean = false,
								private val useDualMessagePayment: Boolean = false,
								val testMode: Boolean = false): Parcelable {
									fun isUseDualMessagePayment(): Boolean {
										return useDualMessagePayment
									}
								}