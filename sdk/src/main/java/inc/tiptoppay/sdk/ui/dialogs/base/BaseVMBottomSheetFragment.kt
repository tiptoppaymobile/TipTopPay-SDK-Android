package inc.tiptoppay.sdk.ui.dialogs.base

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import inc.tiptoppay.sdk.viewmodel.BaseViewModel
import inc.tiptoppay.sdk.viewmodel.BaseViewState

internal abstract class BaseVMBottomSheetFragment<VS: BaseViewState, VM: BaseViewModel<VS>>: BottomSheetDialogFragment() {
	abstract val viewModel: VM
	abstract fun render(state: VS)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.viewState.observe(viewLifecycleOwner, Observer {
			render(it)
		})
	}

	override fun onStart() {
		super.onStart()

		dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
	}
}