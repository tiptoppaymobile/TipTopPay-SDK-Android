package inc.tiptoppay.sdk.ui.dialogs.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.Constraints
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import inc.tiptoppay.sdk.R
import inc.tiptoppay.sdk.viewmodel.BaseViewModel
import inc.tiptoppay.sdk.viewmodel.BaseViewState


internal abstract class BaseVMDialogFragment<VS : BaseViewState, VM : BaseViewModel<VS>> :
	DialogFragment() {
	abstract val viewModel: VM
	abstract fun render(state: VS)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setStyle(STYLE_NO_TITLE, R.style.ttpsdk_Dialog);
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.viewState.observe(viewLifecycleOwner, Observer {
			render(it)
		})
	}

	override fun onStart() {
		super.onStart()

		dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

		dialog?.window?.setLayout(
			Constraints.LayoutParams.MATCH_PARENT,
			Constraints.LayoutParams.WRAP_CONTENT
		) // full width dialog
		dialog?.setCancelable(false)

	}

	override fun onCancel(dialog: DialogInterface) {
		super.onCancel(dialog)
		activity?.finish()
	}
}