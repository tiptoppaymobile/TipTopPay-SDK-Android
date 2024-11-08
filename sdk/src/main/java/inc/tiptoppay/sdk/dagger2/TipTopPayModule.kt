package inc.tiptoppay.sdk.dagger2

import dagger.Component
import dagger.Module
import dagger.Provides
import inc.tiptoppay.sdk.Constants
import inc.tiptoppay.sdk.api.AuthenticationInterceptor
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.api.TipTopPayApiService
import inc.tiptoppay.sdk.api.UserAgentInterceptor
import inc.tiptoppay.sdk.ui.PaymentActivity
import inc.tiptoppay.sdk.viewmodel.PaymentCardViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentFinishViewModel
import inc.tiptoppay.sdk.viewmodel.InstallmentsViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentCashViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentOptionsViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentProcessViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentSpeiViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class TipTopPayModule {
	@Provides
	@Singleton
	fun provideRepository(apiService: TipTopPayApiService)
			= TipTopPayApi(apiService)
}

@Module
class TipTopPayNetModule(private val publicId: String, private var apiUrl: String) {
	@Provides
	@Singleton
	fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
		.setLevel(HttpLoggingInterceptor.Level.BODY)

	@Provides
	@Singleton
	fun providesAuthenticationInterceptor(): AuthenticationInterceptor
			= AuthenticationInterceptor(publicId)

	@Provides
	@Singleton
	fun providesUserAgentInterceptor(): UserAgentInterceptor
			= UserAgentInterceptor(Constants.userAgent)

	@Provides
	@Singleton
	fun provideOkHttpClientBuilder(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient.Builder
			= OkHttpClient.Builder()
		.addInterceptor(loggingInterceptor)

	@Provides
	@Singleton
	fun provideApiService(okHttpClientBuilder: OkHttpClient.Builder, userAgentInterceptor: UserAgentInterceptor,
						  authenticationInterceptor: AuthenticationInterceptor): TipTopPayApiService {
		val client = okHttpClientBuilder
			.addInterceptor(userAgentInterceptor)
			.addInterceptor(authenticationInterceptor)
			.connectTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.followRedirects(false)
			.build()

		val retrofit = Retrofit.Builder()
			.baseUrl(apiUrl)
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.client(client)
			.build()

		return retrofit.create(TipTopPayApiService::class.java)
	}
}

@Singleton
@Component(modules = [TipTopPayModule::class, TipTopPayNetModule::class])
internal interface TipTopPayComponent {
	fun inject(paymentActivity: PaymentActivity)
	fun inject(optionsViewModel: PaymentOptionsViewModel)
	fun inject(cardViewModel: PaymentCardViewModel)
	fun inject(processViewModel: PaymentProcessViewModel)
	fun inject(finishViewModel: PaymentFinishViewModel)
	fun inject(installmentsViewModel: InstallmentsViewModel)
	fun inject(cashViewModel: PaymentCashViewModel)
	fun inject(speiViewModel: PaymentSpeiViewModel)
}
