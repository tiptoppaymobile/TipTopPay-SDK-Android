package inc.tiptoppay.sdk.ui.dialogs.base

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import inc.tiptoppay.sdk.R
import inc.tiptoppay.sdk.configuration.PaymentConfiguration
import inc.tiptoppay.sdk.models.SDKConfiguration
import inc.tiptoppay.sdk.ui.PaymentActivity
import inc.tiptoppay.sdk.viewmodel.BaseViewModel
import inc.tiptoppay.sdk.viewmodel.BaseViewState

internal abstract class BasePaymentDialogFragment<VS: BaseViewState, VM: BaseViewModel<VS>>: BaseVMDialogFragment<VS, VM>() {
	interface IPaymentFragment {
		fun paymentWillFinish()
	}

	private fun getConfiguration(): PaymentConfiguration? {
		if (activity is PaymentActivity) {
			return (activity as PaymentActivity).paymentConfiguration
		}
		return null
	}

	protected val paymentConfiguration by lazy {
		getConfiguration()
	}

	private fun getSDKConfiguration(): SDKConfiguration? {
		if (activity is PaymentActivity) {
			return (activity as PaymentActivity).SDKConfiguration
		}
		return null
	}

	protected val sdkConfig by lazy {
		getSDKConfiguration()
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val fadeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.ttpsdk_fade_in)
		fadeAnim.fillAfter = true

		val slideAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.ttpsdk_slide_in)
		slideAnim.fillAfter = true
	}

	protected fun close(force: Boolean, completion: (() -> (Unit))? = null){
		val slideAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.ttpsdk_slide_out)
		slideAnim.fillAfter = true
		slideAnim.setAnimationListener(object : Animation.AnimationListener{
			override fun onAnimationStart(animation: Animation?) {
			}

			override fun onAnimationEnd(animation: Animation?) {
				animation?.setAnimationListener(null)
				requireActivity().supportFragmentManager.popBackStack()
				if (force) {
					val listener = requireActivity() as? IPaymentFragment
					listener?.paymentWillFinish()
				}
				completion?.invoke()
			}

			override fun onAnimationRepeat(animation: Animation?) {
			}
		})

		val fadeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.ttpsdk_fade_out)
		fadeAnim.fillAfter = true
	}

	fun handleBackButton(){
		close(true)
	}

	internal fun activity(): PaymentActivity {
		return activity as  PaymentActivity
	}

	protected fun errorMode(isErrorMode: Boolean, editText: TextInputEditText){
		if (isErrorMode) {
			editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.ttpsdk_pale_red))
			editText.setBackgroundResource(R.drawable.ttpsdk_bg_edit_text_selector_error)
		} else {
			editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.ttpsdk_dark))
			editText.setBackgroundResource(R.drawable.ttpsdk_bg_edit_text_selector)
		}
	}
}