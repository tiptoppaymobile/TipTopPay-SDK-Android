package inc.tiptoppay.sdk.configuration

import android.os.Parcelable
import inc.tiptoppay.sdk.models.Region
import kotlinx.parcelize.Parcelize
import inc.tiptoppay.sdk.scanner.CardScanner

@Parcelize
data class PaymentConfiguration(val publicId: String,
								val region: Region,
								val paymentData: PaymentData,
								val scanner: CardScanner?,
								val requireEmail: Boolean = false,
								val useDualMessagePayment: Boolean = false,
								val apiUrl: String = "",
								val testMode: Boolean = false): Parcelable