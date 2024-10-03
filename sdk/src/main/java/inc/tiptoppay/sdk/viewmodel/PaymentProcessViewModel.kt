package inc.tiptoppay.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.api.models.InstallmentData
import inc.tiptoppay.sdk.api.models.PaymentRequestBody
import inc.tiptoppay.sdk.api.models.TipTopPayTransaction
import inc.tiptoppay.sdk.api.models.TipTopPayTransactionResponse
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.models.ApiError
import inc.tiptoppay.sdk.models.SDKConfiguration
import inc.tiptoppay.sdk.ui.dialogs.PaymentProcessStatus
import inc.tiptoppay.sdk.util.checkAndGetCorrectJsonDataString
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

internal class PaymentProcessViewModel(
	private val paymentData: PaymentData,
	private val cryptogram: String,
	private val installmentsTerm: Int,
	private val useDualMessagePayment: Boolean,
	private val saveCard: Boolean?

): BaseViewModel<PaymentProcessViewState>() {
	override var currentState = PaymentProcessViewState()
	override val viewState: MutableLiveData<PaymentProcessViewState> by lazy {
		MutableLiveData(currentState)
	}

	private var disposable: Disposable? = null

	@Inject
	lateinit var api: TipTopPayApi

	fun pay() {

		val jsonDataString: String? = checkAndGetCorrectJsonDataString(paymentData.jsonData)

		val body = PaymentRequestBody(amount = paymentData.amount,
									  currency = paymentData.currency.code,
									  name = "",
									  cryptogram = cryptogram,
									  invoiceId = paymentData.invoiceId ?: "",
									  description = paymentData.description ?: "",
									  accountId = paymentData.accountId ?: "",
									  email = paymentData.email ?: "",
									  payer = paymentData.payer,
									  jsonData = jsonDataString)

		if (saveCard != null) {
			body.saveCard = saveCard
		}

		if (installmentsTerm > 1) {
			body.installmentsData = InstallmentData(term = installmentsTerm)
			body.term = installmentsTerm
		}

		if (useDualMessagePayment) {
			disposable = api.auth(body)
				.toObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.map { response ->
					checkTransactionResponse(response)
				}
				.onErrorReturn {
					val state = currentState.copy(status = PaymentProcessStatus.Failed, reasonCode = ApiError.CODE_ERROR_CONNECTION)
					stateChanged(state)
				}
				.subscribe()
		} else {
			disposable = api.charge(body)
				.toObservable()
				.observeOn(AndroidSchedulers.mainThread())
				.map { response ->
					checkTransactionResponse(response)
				}
				.onErrorReturn {
					val state = currentState.copy(status = PaymentProcessStatus.Failed, reasonCode = ApiError.CODE_ERROR_CONNECTION)
					stateChanged(state)
				}
				.subscribe()
		}
	}

	fun postThreeDs(md: String, paRes: String) {
		disposable = api.postThreeDs(md, currentState.transaction?.threeDsCallbackId ?: "", paRes)
			.toObservable()
			.observeOn(AndroidSchedulers.mainThread())
			.map {
				val state: PaymentProcessViewState = if (it.success) {
					currentState.copy(status = PaymentProcessStatus.Succeeded)
				} else {
					currentState.copy(status = PaymentProcessStatus.Failed, reasonCode = it.reasonCode.toString())
				}

				stateChanged(state)
			}
			.onErrorReturn {
				val state = currentState.copy(status = PaymentProcessStatus.Failed, reasonCode = ApiError.CODE_ERROR_CONNECTION)
				stateChanged(state)
			}
			.subscribe()
	}

	fun clearThreeDsData(){
		val state = currentState.copy(acsUrl = null, paReq = null)
		stateChanged(state)
	}

	fun clearQrLinkData(){
		val state = currentState.copy(qrUrl = null)
		stateChanged(state)
	}

	private fun checkTransactionResponse(transactionResponse: TipTopPayTransactionResponse){
		val state = if (transactionResponse.success == true) {
			currentState.copy(
				transaction = transactionResponse.transaction,
				status = PaymentProcessStatus.Succeeded
			)
		} else {
			if (!transactionResponse.message.isNullOrEmpty()) {
				currentState.copy(
					transaction = transactionResponse.transaction,
					status = PaymentProcessStatus.Failed,
					reasonCode = transactionResponse.transaction?.reasonCode.toString()
				)
			} else {
				val paReq = transactionResponse.transaction?.paReq
				val acsUrl = transactionResponse.transaction?.acsUrl

				if (!paReq.isNullOrEmpty() && !acsUrl.isNullOrEmpty()) {
					currentState.copy(
						transaction = transactionResponse.transaction,
						paReq = paReq,
						acsUrl = acsUrl
					)
				} else {
					currentState.copy(
						transaction = transactionResponse.transaction,
						status = PaymentProcessStatus.Failed,
						reasonCode = transactionResponse.transaction?.reasonCode.toString()
					)
				}
			}
		}

		stateChanged(state)
	}

	private fun stateChanged(viewState: PaymentProcessViewState) {
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

internal data class PaymentProcessViewState(
    val status: PaymentProcessStatus = PaymentProcessStatus.InProcess,
    val succeeded: Boolean = false,
    val transaction: TipTopPayTransaction? = null,
    val paReq: String? = null,
    val acsUrl: String? = null,
    val reasonCode: String? = null,
    val qrUrl: String? = null,
    val transactionId: Long? = null
): BaseViewState()