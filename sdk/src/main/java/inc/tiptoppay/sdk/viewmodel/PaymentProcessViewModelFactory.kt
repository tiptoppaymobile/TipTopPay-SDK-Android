package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.tiptoppay.sdk.configuration.PaymentData

internal class PaymentProcessViewModelFactory(
	private val paymentData: PaymentData,
	private val cryptogram: String,
	private val installmentsTerm: Int,
	private val useDualMessagePayment: Boolean,
	private val saveCard: Boolean?
): ViewModelProvider.Factory {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return PaymentProcessViewModel(paymentData, cryptogram, installmentsTerm, useDualMessagePayment, saveCard) as T
	}
}