package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.api.models.QrLinkStatusWaitBody
import inc.tiptoppay.sdk.api.models.QrLinkStatusWaitResponse
import inc.tiptoppay.sdk.api.models.SpeiSendEmailBody
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.configuration.SpeiData
import inc.tiptoppay.sdk.ui.dialogs.EmailFormStatus
import inc.tiptoppay.sdk.ui.dialogs.PaymentSpeiStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

internal class PaymentSpeiViewModel(
    private val paymentData: PaymentData,
    private val speiData: SpeiData
) : BaseViewModel<PaymentSpeiViewState>() {
    override var currentState = PaymentSpeiViewState()
    override val viewState: MutableLiveData<PaymentSpeiViewState> by lazy {
        MutableLiveData(currentState)
    }

    private var disposable: Disposable? = null

    @Inject
    lateinit var api: TipTopPayApi

    override fun onCleared() {
        super.onCleared()

        disposable?.dispose()
    }

    private fun stateChanged(viewState: PaymentSpeiViewState) {
        currentState = viewState.copy()
        this.viewState.apply {
            value = viewState
        }
    }

    fun qrLinkStatusWait() {

        val transactionId = speiData.transactionId

        if (transactionId == null || transactionId == 0L) {
            val state = currentState.copy(status = PaymentSpeiStatus.Failed)
            stateChanged(state)
            return
        }

        val body = QrLinkStatusWaitBody(transactionId)

        disposable = api.qrLinkStatusWait(body)
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                checkQrLinkStatusWaitResponse(response)
            }
            .onErrorReturn {
                val state = currentState.copy(status = PaymentSpeiStatus.Failed)
                stateChanged(state)
            }
            .subscribe()
    }

    fun sendEmail(email: String) {

        val transactionId = speiData.transactionId

        val body = SpeiSendEmailBody(transactionId, email)

        val state = currentState.copy(emailStatus = EmailFormStatus.InProgress)
        stateChanged(state)

        disposable = api.sendEmailForSpei(body)
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if (response.success == true) {
                    val state = currentState.copy(emailStatus = EmailFormStatus.Done)
                    stateChanged(state)
                } else {
                    val state = currentState.copy(emailStatus = EmailFormStatus.Error)
                    stateChanged(state)
                }
            }
            .onErrorReturn {
                val state = currentState.copy(emailStatus = EmailFormStatus.Error)
                stateChanged(state)
            }
            .subscribe()
    }

    fun sendToAnotherEmail() {
        val state = currentState.copy(emailStatus = EmailFormStatus.Form)
        stateChanged(state)
    }

    fun emailDone() {
        val state = currentState.copy(emailStatus = EmailFormStatus.Done)
        stateChanged(state)
    }

    private fun checkQrLinkStatusWaitResponse(response: QrLinkStatusWaitResponse) {

        if (response.success == true) {
            when (response.transaction?.status) {
                "Authorized", "Completed", "Cancelled" -> {
                    val state = currentState.copy(status = PaymentSpeiStatus.Success, transactionId = response.transaction.transactionId)
                    stateChanged(state)
                }
                "Declined" -> {
                    val state = currentState.copy(status = PaymentSpeiStatus.Failed, transactionId = response.transaction.transactionId)
                    stateChanged(state)
                }
                else -> {
                    qrLinkStatusWait()
                }
            }
        } else {
            val state = currentState.copy(status = PaymentSpeiStatus.Failed, transactionId = response.transaction?.transactionId)
            stateChanged(state)
        }
    }
}

internal data class PaymentSpeiViewState(
    val status: PaymentSpeiStatus = PaymentSpeiStatus.InProcess,
    val emailStatus: EmailFormStatus = EmailFormStatus.Form,
    val transactionId: Long? = null,
    val barcodeLink: String? = null,
    val reasonCode: String? = null
) : BaseViewState()