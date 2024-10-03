package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.util.getSha512
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

internal class InstallmentsViewModel: BaseViewModel<InstallmentsViewState>() {
	override var currentState = InstallmentsViewState()
	override val viewState: MutableLiveData<InstallmentsViewState> by lazy {
		MutableLiveData(currentState)
	}

	private var disposable: Disposable? = null

	@Inject lateinit var api: TipTopPayApi

	fun getBinInfo(terminalPublicId: String, cardNumber: String, amount: String, currency: String) {

		if (cardNumber.length < 6) {
			return
		}
		val queryMap = mutableMapOf<String, String>()

		queryMap["TerminalPublicId"] = terminalPublicId

		queryMap["IsCheckCard"] = "true"

		val bin = cardNumber.substring(0, 6)
		queryMap["bin"] = bin

		queryMap["amount"] = amount
		queryMap["currency"] = currency

//		if (cardNumber.length >= 7) {
//			queryMap["sevenNumberHash"] = getSha512(cardNumber.substring(0, 7))
//		}
//
//		if (cardNumber.length >= 8) {
//			queryMap["eightNumberHash"] = getSha512(cardNumber.substring(0, 8))
//		}

		disposable = api.getBinInfo(queryMap)
			.toObservable()
			.observeOn(AndroidSchedulers.mainThread())
			.map {
				if (it.success == true) {
					it.binInfo?.isCardAllowed?.let { isCardAllowed ->
						val state: InstallmentsViewState = currentState.copy(isCardAllowed = isCardAllowed)
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

	private fun stateChanged(viewState: InstallmentsViewState) {
		currentState = viewState.copy()
		this.viewState.apply {
			value = viewState
		}
	}

}

internal data class InstallmentsViewState(
	val isCardAllowed: Boolean = false
): BaseViewState()