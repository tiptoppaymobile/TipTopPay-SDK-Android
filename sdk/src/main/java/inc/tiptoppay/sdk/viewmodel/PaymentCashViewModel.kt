package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.api.models.AltpayPayRequestBody
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.models.ApiError
import inc.tiptoppay.sdk.ui.dialogs.PaymentCashStatus
import inc.tiptoppay.sdk.util.checkAndGetCorrectJsonDataString
import inc.tiptoppay.sdk.util.getRussianLocale
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

internal class PaymentCashViewModel(
    private val paymentData: PaymentData
) : BaseViewModel<PaymentCashViewState>() {
    override var currentState = PaymentCashViewState()
    override val viewState: MutableLiveData<PaymentCashViewState> by lazy {
        MutableLiveData(currentState)
    }

    private var disposable: Disposable? = null

    @Inject
    lateinit var api: TipTopPayApi

    override fun onCleared() {
        super.onCleared()

        disposable?.dispose()
    }

    fun pay(altPayType: String, name: String, email: String, locale: String) {

        val jsonDataString: String? = checkAndGetCorrectJsonDataString(paymentData.getJsonData())

        val payer = paymentData.payer?.copy(firstName = name, email = email)

        val body = AltpayPayRequestBody(
            amount = paymentData.amount,
            currency = paymentData.currency.code,
            altPayType = altPayType,
            invoiceId = paymentData.invoiceId ?: "",
            description = paymentData.description ?: "",
            accountId = paymentData.accountId ?: "",
            email = paymentData.email ?: "",
            payer = payer,
            jsonData = jsonDataString,
            cultureName = locale
        )

        disposable = api.altpayPay(body)
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->

                if (response.success == true) {
                    val state = currentState.copy(
                        status = PaymentCashStatus.BarcodeCreated,
                        transactionId = response.transaction?.transactionId,
                        barcodeLink = response.transaction?.extensionData?.link
                    )
                    stateChanged(state)
                } else {
                    val state = currentState.copy(
                        status = PaymentCashStatus.Failed,
                        reasonCode = response.errorCode
                    )
                    stateChanged(state)
                }
            }
            .onErrorReturn {
                val state = currentState.copy(
                    status = PaymentCashStatus.Failed,
                    reasonCode = ApiError.CODE_ERROR_CONNECTION
                )
                stateChanged(state)
            }
            .subscribe()
    }

    private fun stateChanged(viewState: PaymentCashViewState) {
        currentState = viewState.copy()
        this.viewState.apply {
            value = viewState
        }
    }
}

internal data class PaymentCashViewState(
    val status: PaymentCashStatus = PaymentCashStatus.InProcess,
    val transactionId: Long? = null,
    val barcodeLink: String? = null,
    val reasonCode: String? = null
) : BaseViewState()