package inc.tiptoppay.sdk.configuration

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import inc.tiptoppay.sdk.Constants
import inc.tiptoppay.sdk.api.AuthenticationInterceptor
import inc.tiptoppay.sdk.api.TipTopPayApiService
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.models.Transaction
import inc.tiptoppay.sdk.ui.PaymentActivity
import java.util.concurrent.TimeUnit

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

		fun createApi(publicId: String) = TipTopPayApi(createService(publicId))

		private fun createService(publicId: String): TipTopPayApiService {
			val retrofit = Retrofit.Builder()
				.baseUrl(Constants.baseApiUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.client(createClient(publicId))
				.build()

			return retrofit.create(TipTopPayApiService::class.java)
		}

		private fun createClient(publicId: String?): OkHttpClient {
			val okHttpClientBuilder = OkHttpClient.Builder()
					.addInterceptor(HttpLoggingInterceptor()
											.setLevel(HttpLoggingInterceptor.Level.BODY))
			val client = okHttpClientBuilder
					.connectTimeout(20, TimeUnit.SECONDS)
					.readTimeout(20, TimeUnit.SECONDS)
					.followRedirects(false)

			if (publicId != null){
				client.addInterceptor(AuthenticationInterceptor(publicId))
			}

			return client.build()
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