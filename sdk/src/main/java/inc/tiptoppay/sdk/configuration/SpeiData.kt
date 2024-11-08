package inc.tiptoppay.sdk.configuration

import android.os.Parcelable
import inc.tiptoppay.sdk.api.models.AltpayPayTransaction
import kotlinx.parcelize.Parcelize

@Parcelize
class SpeiData(
	val transactionId: Long,
	val amount: Double,
	var clabe: String,
	val expiredDate: String
) : Parcelable
