package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import inc.tiptoppay.sdk.api.TipTopPayApi
import javax.inject.Inject

internal class PaymentCardViewModel: BaseViewModel<PaymentCardViewState>() {
	override var currentState = PaymentCardViewState()
	override val viewState: MutableLiveData<PaymentCardViewState> by lazy {
		MutableLiveData(currentState)
	}

	private var disposable: Disposable? = null

	@Inject lateinit var api: TipTopPayApi

	override fun onCleared() {
		super.onCleared()

		disposable?.dispose()
	}
}

internal data class PaymentCardViewState(val a: String? = null): BaseViewState()