package inc.tiptoppay.sdk.configuration

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import inc.tiptoppay.sdk.models.Transaction

internal class TipTopPayIntentSender : ActivityResultContract<PaymentConfiguration, Transaction>() {
	override fun createIntent(context: Context, input: PaymentConfiguration): Intent {
		return TipTopPaySDK.getInstance().getStartIntent(context, input)
	}

	override fun parseResult(resultCode: Int, intent: Intent?): Transaction {
		if (resultCode == Activity.RESULT_OK) {
			val id = intent?.getLongExtra(TipTopPaySDK.IntentKeys.TransactionId.name, 0) ?: 0
			val status = intent?.getSerializableExtra(TipTopPaySDK.IntentKeys.TransactionStatus.name) as? TipTopPaySDK.TransactionStatus
			val reasonCode = intent?.getIntExtra(TipTopPaySDK.IntentKeys.TransactionReasonCode.name, 0) ?: 0

			return Transaction(id, status, reasonCode)
		}

		return Transaction(0, null, 0)
	}
}