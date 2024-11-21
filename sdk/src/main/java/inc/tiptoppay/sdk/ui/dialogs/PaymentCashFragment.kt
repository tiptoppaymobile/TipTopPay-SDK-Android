package inc.tiptoppay.sdk.ui.dialogs

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import inc.tiptoppay.sdk.R
import inc.tiptoppay.sdk.databinding.DialogTtpsdkPaymentCashBinding
import inc.tiptoppay.sdk.ui.PaymentActivity
import inc.tiptoppay.sdk.ui.dialogs.base.BasePaymentBottomSheetFragment
import inc.tiptoppay.sdk.util.InjectorUtils
import inc.tiptoppay.sdk.util.TextWatcherAdapter
import inc.tiptoppay.sdk.util.emailIsValid
import inc.tiptoppay.sdk.util.getCurrentLocale
import inc.tiptoppay.sdk.util.payerNameIsValid
import inc.tiptoppay.sdk.viewmodel.PaymentCashViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentCashViewState

internal enum class PaymentCashStatus {
	InProcess,
	BarcodeCreated,
	Failed;
}

internal class PaymentCashFragment :
	BasePaymentBottomSheetFragment<PaymentCashViewState, PaymentCashViewModel>() {
	interface IPaymentCashFragment {
		fun onCashSuccess(success: Boolean, message: String)
	}

	companion object {

		val CASH_OXXO = 0
		val CASH_CSTORES = 1
		val CASH_PHARMACY = 2

		fun newInstance() = PaymentCashFragment().apply {
			arguments = Bundle()
		}
	}

	private var _binding: DialogTtpsdkPaymentCashBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DialogTtpsdkPaymentCashBinding.inflate(inflater, container, false)
		val view = binding.root
		return view
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private var currentState: PaymentCashViewState? = null

	override val viewModel: PaymentCashViewModel by viewModels {
		InjectorUtils.providePaymentCashViewModelFactory(
			paymentConfiguration!!.paymentData)
	}

	override fun render(state: PaymentCashViewState) {

		currentState = state

		val status = currentState?.status

		when (status) {
			PaymentCashStatus.InProcess -> {
				updateStateButtons()
			}
			PaymentCashStatus.BarcodeCreated -> {
				if (state.barcodeLink.isNullOrEmpty()) {
					updateStateButtons()
				} else {

					val url = state.barcodeLink

					val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
					startActivity(browserIntent)
					activity().finish()
				}
			}
			PaymentCashStatus.Failed -> {
				val listener = requireActivity() as? IPaymentCashFragment
				listener?.onCashSuccess(false, currentState?.reasonCode ?: "0")
				dismiss()
			}
			else -> {}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		activity().component.inject(viewModel)

		binding.textTitle.text =
			String.format(
				"%.2f " + paymentConfiguration!!.paymentData.currency.symbol,
				paymentConfiguration!!.paymentData.amount.toDouble()
			)


		val name = paymentConfiguration?.paymentData?.payer?.firstName
		binding.editName.setText(name)

		binding.editName.setOnFocusChangeListener { _, hasFocus ->
			errorMode(
				!hasFocus && !payerNameIsValid(binding.editName.text.toString()),
				binding.editName, binding.tilName
			)
		}

		binding.editName.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)
				updateStateButtons()
			}
		})

		binding.editEmail.setText(paymentConfiguration?.paymentData?.email)

		binding.editEmail.setOnFocusChangeListener { _, hasFocus ->
			errorMode(
				!hasFocus && !emailIsValid(binding.editEmail.text.toString()),
				binding.editEmail, binding.tilEmail
			)
		}

		binding.editEmail.addTextChangedListener(object : TextWatcherAdapter() {
			override fun afterTextChanged(s: Editable?) {
				super.afterTextChanged(s)
				updateStateButtons()
			}
		})

		binding.llForButtons.removeAllViews()

		val inflater = activity?.layoutInflater

		val buttons = sdkConfig?.cashMethods ?: ArrayList()

		for (button in buttons) {
			val locale = getCurrentLocale(requireContext()).toLanguageTag()
			if (button == CASH_OXXO) {
				val layoutOxxo: View? = inflater?.inflate(R.layout.layout_cash_button_oxxo, null)
				binding.llForButtons.addView(layoutOxxo)
				val buttonOxxo = layoutOxxo?.findViewById<View>(R.id.button_oxxo)
				buttonOxxo?.setOnClickListener {

					disableButtons()

					val name = binding.editName.text.toString()
					val email = binding.editEmail.text.toString()
					viewModel.pay("CashOxxo", name, email, locale)
				}
			} else if (button == CASH_CSTORES) {
				val layoutCs: View? = inflater?.inflate(R.layout.layout_cash_button_convenience_store, null)
				binding.llForButtons.addView(layoutCs)
				val buttonCs = layoutCs?.findViewById<View>(R.id.button_cs)
				buttonCs?.setOnClickListener {

					disableButtons()

					val name = binding.editName.text.toString()
					val email = binding.editEmail.text.toString()
					viewModel.pay("CashCStores", name, email, locale)
				}
			}
		}

		updateStateButtons()
	}

	private fun updateStateButtons() {
		if (isValid()) {
			enableButtons()
		} else {
			disableButtons()
		}
	}

	private fun disableButtons() {
		binding.viewBlockButtons.visibility = View.VISIBLE
	}

	private fun enableButtons() {
		binding.viewBlockButtons.visibility = View.GONE
	}

	private fun isValid(): Boolean {
		val nameIsValid = payerNameIsValid(binding.editName.text.toString())
		val emailIsValid = emailIsValid(binding.editEmail.text.toString())

		return nameIsValid && emailIsValid
	}

	override fun onCancel(dialog: DialogInterface) {
		super.onCancel(dialog)
		(activity as PaymentActivity).showPaymentOptions()
	}
}