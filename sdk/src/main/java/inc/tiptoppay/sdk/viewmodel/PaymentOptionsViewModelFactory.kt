package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.tiptoppay.sdk.configuration.PaymentData

internal class PaymentOptionsViewModelFactory(
	private val paymentData: PaymentData,
	private val useDualMessagePayment: Boolean,
): ViewModelProvider.Factory {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return PaymentOptionsViewModel(paymentData, useDualMessagePayment) as T
	}
}