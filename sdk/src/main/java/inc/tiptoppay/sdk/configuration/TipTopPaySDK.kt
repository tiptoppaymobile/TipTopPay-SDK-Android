package inc.tiptoppay.sdk.configuration

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import inc.tiptoppay.sdk.models.Transaction
import inc.tiptoppay.sdk.ui.PaymentActivity

interface TipTopPaySDK {
	fun start(configuration: PaymentConfiguration, from: Activity, requestCode: Int)
	fun launcher(from: AppCompatActivity, result: (Transaction) -> Unit) : ActivityResultLauncher<PaymentConfiguration>
	fun launcher(from: FragmentActivity, result: (Transaction) -> Unit) : ActivityResultLauncher<PaymentConfiguration>
	fun launcher(from: Fragment, result: (Transaction) -> Unit) : ActivityResultLauncher<PaymentConfiguration>

	fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent

	enum class TransactionStatus {
		Succeeded,
		Failed;
	}
	enum class IntentKeys {
		TransactionId,
		TransactionStatus,
		TransactionReasonCode;
	}

	companion object {

		fun getInstance(): TipTopPaySDK {
			return TipTopPaySDKImpl()
		}
	}
}

internal class TipTopPaySDKImpl: TipTopPaySDK {
	override fun start(configuration: PaymentConfiguration, from: Activity, requestCode: Int) {
		from.startActivityForResult(this.getStartIntent(from, configuration), requestCode)
	}

	override fun launcher(
		from: AppCompatActivity,
		result: (Transaction) -> Unit): ActivityResultLauncher<PaymentConfiguration> {
		return from.registerForActivityResult(TipTopPayIntentSender(), result)
	}

	override fun launcher(
		from: FragmentActivity,
		result: (Transaction) -> Unit): ActivityResultLauncher<PaymentConfiguration> {
		return from.registerForActivityResult(TipTopPayIntentSender(), result)
	}

	override fun launcher(
		from: Fragment,
		result: (Transaction) -> Unit
	): ActivityResultLauncher<PaymentConfiguration> {
		return from.registerForActivityResult(TipTopPayIntentSender(), result)
	}

	override fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent {
		return PaymentActivity.getStartIntent(context, configuration)
	}
}