package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.api.models.AltpayPayRequestBody
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.configuration.SpeiData
import inc.tiptoppay.sdk.models.ApiError
import inc.tiptoppay.sdk.ui.dialogs.PaymentOptionsStatus
import inc.tiptoppay.sdk.util.checkAndGetCorrectJsonDataString
import io.reactivex.android.schedulers.AndroidSchedulers
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

    fun getSpeiPaymentData() {

        val jsonDataString: String? = checkAndGetCorrectJsonDataString(paymentData.jsonData)

        val body = AltpayPayRequestBody(amount = paymentData.amount,
            currency = paymentData.currency.code,
            description = paymentData.description ?: "",
            accountId = paymentData.accountId ?: "",
            email = paymentData.email ?: "",
            jsonData = jsonDataString,
            invoiceId = paymentData.invoiceId ?: "",
            altPayType = "Spei",
            payer = paymentData.payer
        )

        val state = currentState.copy(status = PaymentOptionsStatus.SpeiLoading)
        stateChanged(state)

        disposable = api.altpayPay(body)
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                val state = if (response.success == true) {

                    val speiData = SpeiData(
                        transactionId = response.transaction?.transactionId!!,
                        amount = response.transaction?.amount!!,
                        clabe = response.transaction.extensionData?.clabe!!,
                        expiredDate = response.transaction.extensionData?.expiredDate!!)

                    currentState.copy(status = PaymentOptionsStatus.SpeiSuccess,
                        transactionId = response.transaction?.transactionId,
                        speiData = speiData)
                } else {
                    currentState.copy(status = PaymentOptionsStatus.Failed, transactionId = response.transaction?.transactionId)
                }
                stateChanged(state)
            }
            .onErrorReturn {
                val state = currentState.copy(status = PaymentOptionsStatus.Failed, reasonCode = ApiError.CODE_ERROR_CONNECTION)
                stateChanged(state)
            }
            .subscribe()
    }

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
    val isSaveCard: Int? = null,
    val speiData: SpeiData? = null

): BaseViewState()