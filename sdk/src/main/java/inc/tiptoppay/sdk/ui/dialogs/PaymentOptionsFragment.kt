package inc.tiptoppay.sdk.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.google.android.material.checkbox.MaterialCheckBox
import inc.tiptoppay.sdk.R
import inc.tiptoppay.sdk.configuration.SpeiData
import inc.tiptoppay.sdk.databinding.DialogTtpsdkPaymentOptionsBinding
import inc.tiptoppay.sdk.models.ApiError
import inc.tiptoppay.sdk.models.SDKConfiguration
import inc.tiptoppay.sdk.ui.PaymentActivity
import inc.tiptoppay.sdk.ui.dialogs.base.BasePaymentBottomSheetFragment
import inc.tiptoppay.sdk.util.InjectorUtils
import inc.tiptoppay.sdk.util.TextWatcherAdapter
import inc.tiptoppay.sdk.util.emailIsValid
import inc.tiptoppay.sdk.util.hideKeyboard
import inc.tiptoppay.sdk.viewmodel.PaymentOptionsViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentOptionsViewState

internal enum class PaymentOptionsStatus {
	Waiting,
	SpeiLoading,
	SpeiSuccess,
	Failed;
}
internal class PaymentOptionsFragment :
	BasePaymentBottomSheetFragment<PaymentOptionsViewState, PaymentOptionsViewModel>() {
	interface IPaymentOptionsFragment {
		fun runCardPayment()
		fun runInstallments()
		fun runCash()
		fun runSpei(speiData: SpeiData)
		fun runGooglePay()
	}

	companion object {
		fun newInstance() = PaymentOptionsFragment().apply {
			arguments = Bundle()
		}
	}

	private var _binding: DialogTtpsdkPaymentOptionsBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = DialogTtpsdkPaymentOptionsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override val viewModel: PaymentOptionsViewModel by viewModels {
		InjectorUtils.providePaymentOptionsViewModelFactory(
			paymentConfiguration!!.paymentData,
			paymentConfiguration!!.isUseDualMessagePayment())
	}

	override fun render(state: PaymentOptionsViewState) {

		if (sdkConfig?.availablePaymentMethods?.googlePayAvailable == true) {
			binding.buttonGooglepay.root.visibility = View.VISIBLE
		} else {
			binding.buttonGooglepay.root.visibility = View.GONE
		}

		if (sdkConfig?.availablePaymentMethods?.installmentsAvailable == true) {
			binding.buttonInstallments.visibility = View.VISIBLE
		} else {
			binding.buttonInstallments.visibility = View.GONE
		}

		if (sdkConfig?.availablePaymentMethods?.cashAvailable == true) {
			binding.layontButtonCash.visibility = View.VISIBLE
		} else {
			binding.layontButtonCash.visibility = View.GONE
		}

		if (sdkConfig?.availablePaymentMethods?.speiAvailable == true) {
			binding.buttonSpei.visibility = View.VISIBLE
		} else {
			binding.buttonSpei.visibility = View.GONE
		}

		updateWith(state.status, state.reasonCode)
	}

	private fun updateWith(status: PaymentOptionsStatus, errorCode: String? = null) {

		var status = status

		when (status) {
			PaymentOptionsStatus.Waiting -> {
				setSpeiLoading(false)
			}
			PaymentOptionsStatus.SpeiLoading -> {
				setSpeiLoading(true)
				disableAllButtons()
			}
			PaymentOptionsStatus.SpeiSuccess -> {
				setSpeiLoading(false)
				enableAllButtons()
				val listener = requireActivity() as? IPaymentOptionsFragment
				val speiData = viewModel.viewState.value?.speiData
				if (speiData != null) {
					listener?.runSpei(speiData)
					dismiss()
				}
			}
			PaymentOptionsStatus.Failed -> {
				enableAllButtons()

				if (errorCode == ApiError.CODE_ERROR_CONNECTION) {
					val listener = requireActivity() as? PaymentActivity
					listener?.onInternetConnectionError()
					dismiss()
					return
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		activity().component.inject(viewModel)

		if (sdkConfig?.cashMinAmount != null) {
			if (sdkConfig?.cashMinAmount!!.toFloat() > paymentConfiguration?.paymentData?.amount?.toFloat() ?: 0.0f) {
				binding.viewBlockCash.visibility = View.VISIBLE
				binding.textCashNeedMore.visibility = View.VISIBLE
				binding.textCashNeedMore.text = getString(R.string.ttpsdk_text_cash_min_hint) + " " + String.format(
					"%.2f " + paymentConfiguration!!.paymentData.currency.symbol,
					sdkConfig?.cashMinAmount!!.toDouble()
				)
			}
		}

		checkSaveCardState()

		binding.editEmail.setText(paymentConfiguration!!.paymentData.email)

		errorMode(
			!binding.editEmail.hasFocus() && !emailIsValid(binding.editEmail.text.toString()),
			binding.editEmail, binding.textFieldEmail
		)

		binding.editEmail.setOnFocusChangeListener { _, hasFocus ->
			errorMode(
				!hasFocus && !emailIsValid(binding.editEmail.text.toString()),
				binding.editEmail, binding.textFieldEmail
			)
		}

		binding.editEmail.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)
				updateStateButtons()
			}
		})

		if (paymentConfiguration!!.requireEmail) {
			binding.checkboxSendReceipt.visibility = View.GONE
			binding.textFieldEmail.visibility = View.VISIBLE
			binding.textEmailRequire.visibility = View.VISIBLE
		} else {
			binding.checkboxSendReceipt.visibility = View.VISIBLE
			if (paymentConfiguration!!.paymentData.email.isNullOrEmpty()) {
				binding.checkboxSendReceipt.checkedState = MaterialCheckBox.STATE_UNCHECKED
				binding.textFieldEmail.visibility = View.GONE
			} else {
				binding.checkboxSendReceipt.checkedState = MaterialCheckBox.STATE_CHECKED
				binding.textFieldEmail.visibility = View.VISIBLE
			}
			binding.textEmailRequire.visibility = View.GONE
		}

		binding.checkboxSendReceipt.setOnCheckedChangeListener { _, isChecked ->
			binding.textFieldEmail.isGone = !isChecked
			requireActivity().hideKeyboard()
			updateStateButtons()
		}

		updateStateButtons()

		binding.buttonPayCard.setOnClickListener {
			updateEmail()
			updateSaveCard()

			val listener = requireActivity() as? IPaymentOptionsFragment
			listener?.runCardPayment()
			dismiss()
		}

		binding.buttonInstallments.setOnClickListener {
			updateEmail()
			updateSaveCard()

			val listener = requireActivity() as? IPaymentOptionsFragment
			listener?.runInstallments()
			dismiss()
		}

		binding.buttonCash.setOnClickListener {
			updateEmail()
			updateSaveCard()

			val listener = requireActivity() as? IPaymentOptionsFragment
			listener?.runCash()
			dismiss()
		}

		binding.buttonSpei.setOnClickListener {
			updateEmail()
			updateSaveCard()

			viewModel.getSpeiPaymentData()
		}

		binding.buttonGooglepay.root.setOnClickListener {
			updateEmail()
			updateSaveCard()

			val listener = requireActivity() as? IPaymentOptionsFragment
			listener?.runGooglePay()
			dismiss()
		}

		binding.buttonSaveCardPopup.setOnClickListener {
			showPopupSaveCardInfo()
		}

		binding.buttonCardBeSavedPopup.setOnClickListener {
			showPopupSaveCardInfo()
		}
	}

	private fun checkSaveCardState () {

		paymentConfiguration?.paymentData?.accountId?.let { accountId ->
			if (accountId.isNotEmpty()) {
				if (paymentConfiguration?.paymentData?.jsonDataHasRecurrent() == true && sdkConfig?.terminalConfiguration?.isSaveCard == 1) {
					setSaveCardHintVisible()
				}
				if (paymentConfiguration?.paymentData?.jsonDataHasRecurrent() == true && sdkConfig?.terminalConfiguration?.isSaveCard == 2) {
					setSaveCardHintVisible()
				}
				if (paymentConfiguration?.paymentData?.jsonDataHasRecurrent() == false && sdkConfig?.terminalConfiguration?.isSaveCard == 2) {
					setSaveCardCheckBoxVisible()
				}
				if (sdkConfig?.terminalConfiguration?.isSaveCard == 3) {
					setSaveCardHintVisible()
				}
			}
		}
	}

	private fun updateSaveCard() {
		if (binding.checkboxSaveCard.visibility == View.VISIBLE) {
			sdkConfig?.saveCard = binding.checkboxSaveCard.isChecked
		}
	}

	private fun setSaveCardCheckBoxVisible() {
		binding.checkboxSaveCard.visibility = View.VISIBLE
		binding.buttonSaveCardPopup.visibility = View.VISIBLE
		binding.checkboxSaveCard.checkedState = MaterialCheckBox.STATE_UNCHECKED
	}

	private fun setSaveCardHintVisible() {
		binding.textCardBeSaved.visibility = View.VISIBLE
		binding.buttonCardBeSavedPopup.visibility = View.VISIBLE
	}

	private fun showPopupSaveCardInfo() {
		val popupView = layoutInflater.inflate(R.layout.popup_ttpsdk_save_card_info, null)

		val wid = LinearLayout.LayoutParams.WRAP_CONTENT
		val high = LinearLayout.LayoutParams.WRAP_CONTENT
		val focus= true
		val popupWindow = PopupWindow(popupView, wid, high, focus)

		val background = activity?.let { ContextCompat.getDrawable(it, R.drawable.ttpsdk_bg_popup) }
		popupView.background = background

		popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
	}

	private fun updateEmail() {
		if (paymentConfiguration!!.requireEmail || binding.checkboxSendReceipt.isChecked) {
			paymentConfiguration?.paymentData?.email = binding.editEmail.text.toString()
		} else {
			paymentConfiguration?.paymentData?.email = ""
		}
	}

	private fun updateStateButtons() {
		if (paymentConfiguration!!.requireEmail) {
			if (isValid()) {
				enableAllButtons()
			} else {
				disableAllButtons()
			}
		} else {
			if (binding.checkboxSendReceipt.checkedState == MaterialCheckBox.STATE_CHECKED) {
				if (isValid()) {
					enableAllButtons()
				} else {
					disableAllButtons()
				}
			} else {
				enableAllButtons()
			}
		}
	}
	private fun isValid(): Boolean {

		val valid = if (paymentConfiguration!!.requireEmail) {
			emailIsValid(binding.editEmail.text.toString())
		} else {
			!binding.checkboxSendReceipt.isChecked || emailIsValid(binding.editEmail.text.toString())
		}

		return valid
	}

	private fun setSpeiLoading(isLoading: Boolean) {
		if (isLoading) {
			binding.buttonSpeiLogo.visibility = View.GONE
			binding.buttonSpeiProgress.visibility = View.VISIBLE
		} else {
			binding.buttonSpeiLogo.visibility = View.VISIBLE
			binding.buttonSpeiProgress.visibility = View.GONE
		}
	}

	private fun disableAllButtons() {
		binding.viewBlockButtons.visibility = View.VISIBLE
	}

	private fun enableAllButtons() {
		binding.viewBlockButtons.visibility = View.GONE
	}

	override fun onCancel(dialog: DialogInterface) {
		super.onCancel(dialog)
		activity?.finish()
	}
}