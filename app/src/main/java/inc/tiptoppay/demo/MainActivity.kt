package inc.tiptoppay.demo

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import inc.tiptoppay.demo.models.SdkParameter
import inc.tiptoppay.demo.ui.MainScreen
import inc.tiptoppay.sdk.configuration.PaymentConfiguration
import inc.tiptoppay.sdk.configuration.TipTopPaySDK

class MainActivity : ComponentActivity() {

	private lateinit var ttpSdkLauncher: ActivityResultLauncher<PaymentConfiguration>

	@RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		ttpSdkLauncher = TipTopPaySDK.getInstance().launcher(this, result = {
		if (it.status != null) {
			if (it.status == TipTopPaySDK.TransactionStatus.Succeeded) {
				Toast.makeText(this, "Успешно! Транзакция №${it.transactionId}", Toast.LENGTH_SHORT).show()
			} else {
				if (it.reasonCode != 0) {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}. Код ошибки ${it.reasonCode}", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}.", Toast.LENGTH_SHORT).show()
				}
			}
		}
	})

		setContent {
			val context = LocalContext.current
			val colorScheme = if (isSystemInDarkTheme())
                dynamicDarkColorScheme(context)
			else
                dynamicLightColorScheme(context)

            MaterialTheme(colorScheme = colorScheme) {
                Surface {
                    MainScreen(
                        onRunSdk = { parameters ->
                            runTtpSdk(parameters)
                        }
                    )
                }
            }
		}
	}

	private fun runTtpSdk(parameters: List<SdkParameter>) {
		try {
			val configuration = SdkConfigurator.buildConfiguration(parameters)
			ttpSdkLauncher.launch(configuration)
		} catch (e: Exception) {
			Toast.makeText(this, "Ошибка запуска SDK: ${e.message}", Toast.LENGTH_LONG).show()
		}
	}
}