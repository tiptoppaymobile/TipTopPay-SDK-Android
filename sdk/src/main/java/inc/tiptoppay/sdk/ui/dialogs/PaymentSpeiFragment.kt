package inc.tiptoppay.sdk.ui.dialogs

import android.app.Dialog
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import inc.tiptoppay.sdk.R
import inc.tiptoppay.sdk.configuration.SpeiData
import inc.tiptoppay.sdk.databinding.DialogTtpsdkPaymentSpeiBinding
import inc.tiptoppay.sdk.ui.dialogs.EmailFormStatus.*
import inc.tiptoppay.sdk.ui.dialogs.base.BasePaymentDialogFragment
import inc.tiptoppay.sdk.util.InjectorUtils
import inc.tiptoppay.sdk.util.emailIsValid
import inc.tiptoppay.sdk.viewmodel.PaymentSpeiViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentSpeiViewState
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


internal enum class PaymentSpeiStatus {
	InProcess,
	Success,
	Failed
}

internal enum class EmailFormStatus {
	Done,
	Form,
	InProgress,
	Error
}

internal class PaymentSpeiFragment :
	BasePaymentDialogFragment<PaymentSpeiViewState, PaymentSpeiViewModel>() {
	interface IPaymentSpeiFragment {
		fun onSpeiSuccess(success: Boolean, message: String)
		fun onSpeiBackPressed()
	}

	companion object {

		private const val ARG_SPEI_DATA = "arg_spei_data"

		fun newInstance(speiData: SpeiData) = PaymentSpeiFragment().apply {
			arguments = Bundle()
			arguments?.putParcelable(ARG_SPEI_DATA, speiData)
		}
	}

	private val speiData by lazy {
		arguments?.getParcelable(ARG_SPEI_DATA) as SpeiData?
	}

	private var _binding: DialogTtpsdkPaymentSpeiBinding? = null

	private val binding get() = _binding!!

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return object : Dialog(requireActivity(), theme) {
			override fun onBackPressed() {
				dismiss()
				val listener = requireActivity() as? IPaymentSpeiFragment
				listener?.onSpeiBackPressed()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		isCancelable = true

		setStyle(STYLE_NORMAL, R.style.ttpsdk_FullScreenDialog)

		if (paymentConfiguration?.paymentData?.email.isNullOrEmpty()) {
			viewModel.sendToAnotherEmail()
		} else {
			viewModel.emailDone()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = DialogTtpsdkPaymentSpeiBinding.inflate(inflater, container, false)
		val view = binding.root
		return view
	}

	override fun onStart() {
		super.onStart()
		val dialog = dialog
		if (dialog != null) {
			val width = ViewGroup.LayoutParams.MATCH_PARENT
			val height = ViewGroup.LayoutParams.MATCH_PARENT
			dialog.window!!.setLayout(width, height)
			dialog.window!!.setWindowAnimations(R.style.ttpsdk_Slide)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private var currentState: PaymentSpeiViewState? = null

	override val viewModel: PaymentSpeiViewModel by viewModels {
		InjectorUtils.providePaymentSpeiViewModelFactory(
			paymentConfiguration!!.paymentData, speiData!!)
	}

	override fun render(state: PaymentSpeiViewState) {

		currentState = state

		val status = currentState?.status

		when (status) {
			PaymentSpeiStatus.InProcess -> {
				viewModel.qrLinkStatusWait()
			}
			PaymentSpeiStatus.Success -> {
				val listener = requireActivity() as? IPaymentSpeiFragment
				listener?.onSpeiSuccess(true, "")
				dismiss()
			}
			PaymentSpeiStatus.Failed -> {
				val listener = requireActivity() as? IPaymentSpeiFragment
				listener?.onSpeiSuccess(false, currentState?.reasonCode ?: "0")
				dismiss()
			}
			else -> {}
		}

		val emailStatus = currentState?.emailStatus

		when (emailStatus) {
			Done -> {
				showEmailDoneForm()
			}
			Form -> {
				showEmailSendForm()
			}
			InProgress -> {
				showEmailSendFormProgress()
			}
			Error -> {
				showEmailSendFormError()
			}
			else -> {}
		}

		if (state.barcodeLink.isNullOrEmpty()) {

		} else {
			val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(state.barcodeLink))
			startActivity(browserIntent)
			activity().finish()
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

		binding.editClabe.setText(speiData?.clabe)
		binding.editBank.setText("STP")
		binding.editAmount.setText(String.format(
			"%.2f " + paymentConfiguration!!.paymentData.currency.symbol,
			speiData?.amount))
		binding.editPaymentConcept.setText(sdkConfig?.terminalConfiguration?.terminalName)

		binding.buttonClabeCopy.setOnClickListener {
			copyText(binding.editClabe.text.toString())
		}

		binding.buttonBankCopy.setOnClickListener {
			copyText(binding.editBank.text.toString())
		}

		binding.buttonAmountCopy.setOnClickListener {
			copyText(binding.editAmount.text.toString())
		}

		binding.buttonPaymentConceptCopy.setOnClickListener {
			copyText(binding.editPaymentConcept.text.toString())
		}

		val format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
		val expDate: Date = format.parse(speiData?.expiredDate)
		val printFormat: DateFormat = SimpleDateFormat("MMM dd 'at' HH:mm")
		binding.textExpDate.setText(printFormat.format(expDate))

		if (paymentConfiguration!!.paymentData.email.isNullOrEmpty()) {
			showEmailSendForm()
		} else {
			showEmailDoneForm()
			binding.textEmail.text = paymentConfiguration!!.paymentData.email
		}

		binding.textOtherMetod.setOnClickListener {
			dismiss()
			val listener = requireActivity() as? IPaymentSpeiFragment
			listener?.onSpeiBackPressed()
		}

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

		binding.buttonSendEmail.setOnClickListener {

			val email = binding.editEmail.text.toString()

			if (emailIsValid(email)) {
				viewModel.sendEmail(email)
			}
		}
	}

	private fun showEmailDoneForm() {
		binding.llEmailSend.visibility = View.GONE
		binding.llEmailDone.visibility = View.VISIBLE

		val newEmail = binding.editEmail.text.toString()

		if (emailIsValid(newEmail)) {
			binding.textEmail.text = newEmail
		}

		binding.buttonSendToAnotherEmail.setOnClickListener {
			viewModel.sendToAnotherEmail()
		}
	}

	private fun showEmailSendForm() {
		binding.llEmailSend.visibility = View.VISIBLE
		binding.llEmailDone.visibility = View.GONE

		binding.llEmailError.visibility = View.GONE
		binding.imageSendIcon.visibility = View.VISIBLE
		binding.buttonEmailProgress.visibility = View.GONE
	}

	private fun showEmailSendFormProgress() {
		binding.llEmailSend.visibility = View.VISIBLE
		binding.llEmailDone.visibility = View.GONE

		binding.llEmailError.visibility = View.GONE
		binding.imageSendIcon.visibility = View.GONE
		binding.buttonEmailProgress.visibility = View.VISIBLE
	}

	private fun showEmailSendFormError() {
		binding.llEmailSend.visibility = View.VISIBLE
		binding.llEmailDone.visibility = View.GONE

		binding.llEmailError.visibility = View.VISIBLE
		binding.imageSendIcon.visibility = View.VISIBLE
		binding.buttonEmailProgress.visibility = View.GONE
	}

	private fun copyText(text: String) {
		val clipboardManager =
			requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

		val clipData = ClipData.newPlainText(
			null,
			text
		)
			.apply {
				description.extras = PersistableBundle().apply {
					putBoolean(ClipDescription.MIMETYPE_TEXT_PLAIN, true)
				}
			}

		clipboardManager.setPrimaryClip(clipData)
		Toast.makeText(requireActivity(), "Copied", Toast.LENGTH_SHORT).show()
	}
}