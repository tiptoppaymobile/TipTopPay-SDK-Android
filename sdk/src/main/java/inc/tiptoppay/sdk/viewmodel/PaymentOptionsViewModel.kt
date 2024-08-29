package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.ui.dialogs.PaymentOptionsStatus
import io.reactivex.disposables.Disposable
import javax.inject.Inject

internal class PaymentOptionsViewModel(
    private val paymentData: PaymentData,
    private val useDualMessagePayment: Boolean
): BaseViewModel<PaymentOptionsViewState>() {
    override var currentState = PaymentOptionsViewState()
    override val viewState: MutableLiveData<PaymentOptionsViewState> by lazy {
        MutableLiveData(currentState)
    }

    private var disposable: Disposable? = null

    @Inject
    lateinit var api: TipTopPayApi

    private fun stateChanged(viewState: PaymentOptionsViewState) {
        currentState = viewState.copy()
        this.viewState.apply {
            value = viewState
        }
    }

    override fun onCleared() {
        super.onCleared()

        disposable?.dispose()
    }
}

internal data class PaymentOptionsViewState(
    val status: PaymentOptionsStatus = PaymentOptionsStatus.Waiting,
    val reasonCode: String? = null,
    val qrUrl: String? = null,
    val transactionId: Long? = null,
    val isSaveCard: Int? = null

): BaseViewState()