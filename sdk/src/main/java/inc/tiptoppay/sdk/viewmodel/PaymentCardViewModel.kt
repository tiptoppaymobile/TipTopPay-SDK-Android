package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.card.Card
import inc.tiptoppay.sdk.util.getSha512
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

internal class PaymentCardViewModel: BaseViewModel<PaymentCardViewState>() {
	override var currentState = PaymentCardViewState()
	override val viewState: MutableLiveData<PaymentCardViewState> by lazy {
		MutableLiveData(currentState)
	}

	private var disposable: Disposable? = null

	@Inject lateinit var api: TipTopPayApi

	fun getBinInfo(cardNumber: String) {

		if (cardNumber.length !in 6..8) {
			val state = currentState.copy(isKaspiBank = false)
			stateChanged(state)
			return
		}
		val queryMap = mutableMapOf<String, String>()

		val bin = cardNumber.substring(0, 6)
		queryMap["bin"] = bin

		if (cardNumber.length >= 7) {
			queryMap["sevenNumberHash"] = getSha512(cardNumber.substring(0, 7))
		}

		if (cardNumber.length >= 8) {
			queryMap["eightNumberHash"] = getSha512(cardNumber.substring(0, 8))
		}

		disposable = api.getBinInfo(queryMap)
			.toObservable()
			.observeOn(AndroidSchedulers.mainThread())
			.map {
				if (it.success) {
					it.binInfo?.bankName.let { bankName ->
						val isKaspi = Card.isKaspiBank(bankName ?: "")
						val state = currentState.copy(isKaspiBank = isKaspi)
						stateChanged(state)
					}
				} else {
					// leave it
				}
			}
			.onErrorReturn {
				// leave it
			}
			.subscribe()
	}

	override fun onCleared() {
		super.onCleared()

		disposable?.dispose()
	}

	private fun stateChanged(viewState: PaymentCardViewState) {
		currentState = viewState.copy()
		this.viewState.apply {
			value = viewState
		}
	}
}

internal data class PaymentCardViewState(val isKaspiBank: Boolean = false): BaseViewState()