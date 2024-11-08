package inc.tiptoppay.sdk.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import inc.tiptoppay.sdk.R
import inc.tiptoppay.sdk.api.models.TipTopPayTransaction
import inc.tiptoppay.sdk.databinding.DialogTtpsdkPaymentFinishBinding
import inc.tiptoppay.sdk.models.ApiError
import inc.tiptoppay.sdk.ui.dialogs.base.BasePaymentDialogFragment
import inc.tiptoppay.sdk.util.InjectorUtils
import inc.tiptoppay.sdk.viewmodel.PaymentFinishViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentFinishViewState

internal enum class PaymentFinishStatus {
	Successed,
	Failed;
}

internal class PaymentFinishFragment: BasePaymentDialogFragment<PaymentFinishViewState, PaymentFinishViewModel>() {
	interface IPaymentFinishFragment {
		fun finishPayment()
		fun retryPayment()
	}

	companion object {

		private const val ARG_STATUS = "arg_status"
		private const val ARG_TRANSACTION = "arg_transaction"
		private const val ARG_REASON_CODE = "arg_reason_code"
		private const val ARG_RETRY_PAYMENT = "arg_retry_payment"

		fun newInstance(status: PaymentFinishStatus) = PaymentFinishFragment().apply {
			arguments = Bundle()
			arguments?.putString(ARG_STATUS, status.toString())
		}

		fun newInstance(status: PaymentFinishStatus, reasonCode: String, retryPayment: Boolean) = PaymentFinishFragment().apply {
			arguments = Bundle()
			arguments?.putString(ARG_STATUS, status.toString())
			arguments?.putString(ARG_REASON_CODE, reasonCode)
			arguments?.putBoolean(ARG_RETRY_PAYMENT, retryPayment)
		}

		fun newInstance(status: PaymentFinishStatus, transaction: TipTopPayTransaction, reasonCode: String, retryPayment: Boolean) = PaymentFinishFragment().apply {
			arguments = Bundle()
			arguments?.putString(ARG_STATUS, status.toString())
			arguments?.putParcelable(ARG_TRANSACTION, transaction)
			arguments?.putString(ARG_REASON_CODE, reasonCode)
			arguments?.putBoolean(ARG_RETRY_PAYMENT, retryPayment)
		}
	}

	private var _binding: DialogTtpsdkPaymentFinishBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DialogTtpsdkPaymentFinishBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private var currentState: PaymentFinishViewState? = null

	override val viewModel: PaymentFinishViewModel by viewModels {
		InjectorUtils.providePaymentFinishViewModelFactory(
			status,
			transaction,
			reasonCode)
	}

	override fun render(state: PaymentFinishViewState) {
	}

	private val status by lazy {
		val stringStatus = arguments?.getString(ARG_STATUS) ?: ""
		try {
			PaymentFinishStatus.valueOf(stringStatus)
		} catch(e: IllegalArgumentException) {
			PaymentFinishStatus.Failed
		}

	}

	private val transaction by lazy {
		when {
			android.os.Build.VERSION.SDK_INT >= 33 -> arguments?.getParcelable(ARG_TRANSACTION, TipTopPayTransaction::class.java) ?: null
			else -> @Suppress("DEPRECATION") arguments?.getParcelable(ARG_TRANSACTION) ?: null
		}
	}

	private val reasonCode by lazy {
		arguments?.getString(ARG_REASON_CODE) ?: ""
	}

	private val retryPayment by lazy {
		arguments?.getBoolean(ARG_RETRY_PAYMENT) ?: false
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		if (savedInstanceState == null) {
			activity().component.inject(viewModel)
			updateWith(status)
		}
	}

	private fun updateWith(status: PaymentFinishStatus) {

		binding.buttonFinish.isInvisible = false
		binding.buttonFinish.setBackgroundResource(R.drawable.ttpsdk_bg_rounded_blue_button)
		binding.buttonFinish.setTextColor(context?.let { ContextCompat.getColor(it, R.color.ttpsdk_white) } ?: 0xFFFFFF)

		val listener = requireActivity() as? IPaymentFinishFragment

		when (status) {
			PaymentFinishStatus.Successed -> {
				binding.iconStatus.setImageResource(R.drawable.ttpsdk_ic_success)
				binding.textStatus.setText(R.string.ttpsdk_text_process_title_success)
				binding.textDescription.text = ""
				binding.buttonFinish.setText(R.string.ttpsdk_text_process_button_success)
				binding.buttonFinish.setOnClickListener {
					listener?.finishPayment()
					dismiss()
				}
			}

			PaymentFinishStatus.Failed -> {
					binding.iconStatus.setImageResource(R.drawable.ttpsdk_ic_failure)
					binding.textStatus.text =
						context?.let { ApiError.getErrorDescription(it, reasonCode) }
					binding.textDescription.text =
						context?.let { ApiError.getErrorDescriptionExtra(it, reasonCode) }
				if (retryPayment) {
					binding.buttonFinish.setText(R.string.ttpsdk_text_process_button_error)
					binding.buttonFinish.setOnClickListener {
						listener?.retryPayment()
						dismiss()
					}
				} else {
					binding.buttonFinish.setText(R.string.ttpsdk_text_process_button_ok)
					binding.buttonFinish.setOnClickListener {
						listener?.finishPayment()
						dismiss()
						activity?.finish()
					}
				}
			}
		}
	}
}