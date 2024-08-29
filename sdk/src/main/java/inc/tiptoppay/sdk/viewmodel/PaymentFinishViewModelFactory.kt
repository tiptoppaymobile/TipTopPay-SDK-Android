package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.tiptoppay.sdk.api.models.TipTopPayTransaction
import inc.tiptoppay.sdk.ui.dialogs.PaymentFinishStatus

internal class PaymentFinishViewModelFactory(
    val status: PaymentFinishStatus,
    val transaction: TipTopPayTransaction? = null,
    val reasonCode: String? = null
): ViewModelProvider.Factory {

	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return PaymentFinishViewModel(status,
									  transaction,
									  reasonCode) as T
	}
}