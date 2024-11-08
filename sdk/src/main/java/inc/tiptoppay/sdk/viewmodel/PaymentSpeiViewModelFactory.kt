package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.configuration.SpeiData

internal class PaymentSpeiViewModelFactory(
	private val paymentData: PaymentData,
	private val speiData: SpeiData
): ViewModelProvider.Factory {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return PaymentSpeiViewModel(paymentData, speiData) as T
	}
}