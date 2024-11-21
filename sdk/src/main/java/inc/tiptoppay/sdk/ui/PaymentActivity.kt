package inc.tiptoppay.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import inc.tiptoppay.sdk.R
import inc.tiptoppay.sdk.api.TipTopPayApi
import inc.tiptoppay.sdk.api.models.ExternalPaymentMethods
import inc.tiptoppay.sdk.configuration.PaymentConfiguration
import inc.tiptoppay.sdk.configuration.SpeiData
import inc.tiptoppay.sdk.configuration.TipTopPaySDK
import inc.tiptoppay.sdk.dagger2.TipTopPayComponent
import inc.tiptoppay.sdk.dagger2.TipTopPayModule
import inc.tiptoppay.sdk.dagger2.TipTopPayNetModule
import inc.tiptoppay.sdk.databinding.ActivityTtpsdkPaymentBinding
import inc.tiptoppay.sdk.models.ApiError
import inc.tiptoppay.sdk.models.SDKConfiguration
import inc.tiptoppay.sdk.ui.dialogs.InstallmentsFragment
import inc.tiptoppay.sdk.ui.dialogs.PaymentCardFragment
import inc.tiptoppay.sdk.ui.dialogs.PaymentCashFragment
import inc.tiptoppay.sdk.ui.dialogs.PaymentFinishFragment
import inc.tiptoppay.sdk.ui.dialogs.PaymentFinishStatus
import inc.tiptoppay.sdk.ui.dialogs.PaymentOptionsFragment
import inc.tiptoppay.sdk.ui.dialogs.PaymentProcessFragment
import inc.tiptoppay.sdk.ui.dialogs.PaymentSpeiFragment
import inc.tiptoppay.sdk.ui.dialogs.base.BasePaymentBottomSheetFragment
import inc.tiptoppay.sdk.util.GooglePayHandler
import inc.tiptoppay.sdk.util.nextFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

internal class PaymentActivity : FragmentActivity(),
    BasePaymentBottomSheetFragment.IPaymentFragment,
    PaymentOptionsFragment.IPaymentOptionsFragment, PaymentCardFragment.IPaymentCardFragment,
    PaymentProcessFragment.IPaymentProcessFragment, InstallmentsFragment.IInstallmentsFragment,
    PaymentCashFragment.IPaymentCashFragment, PaymentSpeiFragment.IPaymentSpeiFragment,
    PaymentFinishFragment.IPaymentFinishFragment {

    val SDKConfiguration: SDKConfiguration = SDKConfiguration()

    private var disposable: Disposable? = null

    @Inject
    lateinit var api: TipTopPayApi


    companion object {
        private const val REQUEST_CODE_GOOGLE_PAY = 1
        private const val EXTRA_CONFIGURATION = "EXTRA_CONFIGURATION"

        fun getStartIntent(context: Context, configuration: PaymentConfiguration): Intent {
            val intent = Intent(context, PaymentActivity::class.java)
            intent.putExtra(EXTRA_CONFIGURATION, configuration)
            return intent
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.ttpsdk_fade_in, R.anim.ttpsdk_fade_out)
    }

    internal val component: TipTopPayComponent by lazy {

        val apiUrl = if (paymentConfiguration!!.apiUrl.isNullOrEmpty()) {
            paymentConfiguration!!.region.apiUrl
        } else {
            paymentConfiguration!!.apiUrl
        }

        inc.tiptoppay.sdk.dagger2.DaggerTipTopPayComponent
            .builder()
            .tipTopPayModule(TipTopPayModule())
            .tipTopPayNetModule(TipTopPayNetModule(paymentConfiguration!!.publicId, apiUrl))
            .build()
    }

    val paymentConfiguration by lazy {
        intent.getParcelableExtra<PaymentConfiguration>(EXTRA_CONFIGURATION)
    }

    private lateinit var binding: ActivityTtpsdkPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTtpsdkPaymentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        component.inject(this)

        getPublicKey()
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable?.dispose()
        disposable = null
    }

    private fun getPublicKey() {
        disposable = api.getPublicKey()
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->

                SDKConfiguration.publicKey.pem = response.pem
                SDKConfiguration.publicKey.version = response.version

                paymentConfiguration?.let { getMerchantConfiguration(it.publicId) }
            }
            .onErrorReturn {
                onInternetConnectionError()
            }
            .subscribe()
    }

    private fun getMerchantConfiguration(publicId: String) {
        disposable = api.getMerchantConfiguration(publicId)
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if (response.success == true) {
                    
					var isGooglePayAvailable = false
					var isInstallmentsAvailable = false
					var isCashAvailable = false
					var isSpeiAvailable = false

					for (paymentMethod in response.model?.externalPaymentMethods!!) {
						if (paymentMethod.type == ExternalPaymentMethods.GOOGLE_PAY) {
							isGooglePayAvailable = paymentMethod.enabled ?: false
							if (!paymentMethod.gPayGatewayName.isNullOrBlank()) {
								SDKConfiguration.terminalConfiguration.gPayGatewayName =
									paymentMethod.gPayGatewayName
							}
						}
						if (paymentMethod.type == ExternalPaymentMethods.INSTALLMENTS) {
							isInstallmentsAvailable = paymentMethod.enabled ?: false
						}
						if (paymentMethod.type == ExternalPaymentMethods.CASH) {
							isCashAvailable = paymentMethod.enabled ?: false
							SDKConfiguration.cashMinAmount = paymentMethod.cashMinAmount ?: 0
							SDKConfiguration.cashMethods = paymentMethod.cashMethods ?: ArrayList()
						}
						if (paymentMethod.type == ExternalPaymentMethods.SPEI) {
							isSpeiAvailable = paymentMethod.enabled ?: false
						}
					}

					SDKConfiguration.availablePaymentMethods.googlePayAvailable = isGooglePayAvailable
					SDKConfiguration.availablePaymentMethods.cashAvailable = isCashAvailable
					SDKConfiguration.availablePaymentMethods.speiAvailable = isSpeiAvailable

                    SDKConfiguration.terminalConfiguration.isSaveCard =
                        response.model?.features?.isSaveCard
                    SDKConfiguration.terminalConfiguration.skipExpiryValidation =
                        response.model?.skipExpiryValidation
                    SDKConfiguration.terminalConfiguration.terminalName = response.model.terminalName

                    if (isInstallmentsAvailable) {
                        getInstallmentsConfiguration(
                            paymentConfiguration?.publicId!!,
                            paymentConfiguration?.paymentData?.amount!!
                        )
                    } else {
                        SDKConfiguration.availablePaymentMethods.installmentsAvailable = false
                        prepareGooglePay()
                    }
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .onErrorReturn {
                onInternetConnectionError()
            }
            .subscribe()
    }

    private fun getInstallmentsConfiguration(publicId: String, amount: String) {
        disposable = api.getInstallmentsConfiguration(publicId, amount)
            .toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .map { response ->
                if (response.success == true) {

                    if (response.model?.isCardInstallmentAvailable == true) {
                        SDKConfiguration.availablePaymentMethods.installmentsAvailable = true
                        SDKConfiguration.installmentsVariant = response.model.configuration!!
                    }
                }

                prepareGooglePay()
            }
            .onErrorReturn {
                onInternetConnectionError()
            }
            .subscribe()
    }

    private fun prepareGooglePay() {

        if (!SDKConfiguration.availablePaymentMethods.googlePayAvailable) {
            showUi()
            return
        }

        if (supportFragmentManager.backStackEntryCount == 0) {
            GooglePayHandler.isReadyToMakeGooglePay(this)
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    SDKConfiguration.availablePaymentMethods.googlePayAvailable = it
                    showUi()
                }
                .onErrorReturn {
                    SDKConfiguration.availablePaymentMethods.googlePayAvailable = false
                    showUi()
                }
                .subscribe()
        }
    }

    private fun showUi() {
        binding.layoutProgress.isVisible = false
        val fragment = PaymentOptionsFragment.newInstance()
        fragment.show(supportFragmentManager, "")
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_content)
        if (fragment is BasePaymentBottomSheetFragment<*, *>) {
            fragment.handleBackButton()
        } else {
            super.onBackPressed()
        }
    }

    override fun runCardPayment() {
        val fragment = PaymentCardFragment.newInstance()
        fragment.show(supportFragmentManager, "")
    }

    override fun runInstallments() {
        val fragment = InstallmentsFragment.newInstance()
        fragment.show(supportFragmentManager, "")
    }

    override fun runCash() {
        val fragment = PaymentCashFragment.newInstance()
        fragment.show(supportFragmentManager, "")
    }
    
	override fun runSpei(speiData: SpeiData) {
		val fragment = PaymentSpeiFragment.newInstance(speiData)
		fragment.show(supportFragmentManager, "")
	}

	override fun runGooglePay() {
		GooglePayHandler.present(
            paymentConfiguration!!, 
            SDKConfiguration.terminalConfiguration.gPayGatewayName,
            this, 
            REQUEST_CODE_GOOGLE_PAY
        )
	}

    override fun onPayCardClicked(cryptogram: String) {
        val fragment = PaymentProcessFragment.newInstance(cryptogram)
        fragment.show(supportFragmentManager, "")
    }

    override fun onPayInstallmentsClicked(cryptogram: String, term: Int) {
        val fragment = PaymentProcessFragment.newInstance(cryptogram, term)
        fragment.show(supportFragmentManager, "")
    }


    override fun onPaymentFinished(transactionId: Long) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(TipTopPaySDK.IntentKeys.TransactionId.name, transactionId)
            putExtra(
                TipTopPaySDK.IntentKeys.TransactionStatus.name,
                TipTopPaySDK.TransactionStatus.Succeeded
            )
        })
    }

    override fun onPaymentFailed(transactionId: Long, reasonCode: Int?) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(TipTopPaySDK.IntentKeys.TransactionId.name, transactionId)
            putExtra(
                TipTopPaySDK.IntentKeys.TransactionStatus.name,
                TipTopPaySDK.TransactionStatus.Failed
            )
            reasonCode?.let { putExtra(TipTopPaySDK.IntentKeys.TransactionReasonCode.name, it) }
        })
    }

    fun onInternetConnectionError() {
        val fragment = PaymentFinishFragment.newInstance(
            PaymentFinishStatus.Failed,
            ApiError.CODE_ERROR_CONNECTION,
            false
        )
        fragment.show(supportFragmentManager, "")
    }

    override fun finishPayment() {
        finish()
    }

    override fun retryPayment() {
        setResult(Activity.RESULT_CANCELED, Intent())
        showUi()
    }

    override fun paymentWillFinish() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_GOOGLE_PAY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        handleGooglePaySuccess(data)
                    }
                    Activity.RESULT_CANCELED, AutoResolveHelper.RESULT_ERROR -> {
                        handleGooglePayFailure(data)
                    }
                    else -> super.onActivityResult(requestCode, resultCode, data)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleGooglePaySuccess(intent: Intent?) {
        if (intent != null) {
            val paymentData = PaymentData.getFromIntent(intent)
            val token = paymentData?.paymentMethodToken?.token

            if (token != null) {
                val runnable = {
                    val fragment = PaymentProcessFragment.newInstance(token)
                    nextFragment(fragment, true, R.id.frame_content)
                }
                Handler().postDelayed(runnable, 1000)
            }
        }
    }

    private fun handleGooglePayFailure(intent: Intent?) {
        finish()
    }

    override fun onCashSuccess(success: Boolean, message: String) {
        if (success) {
            val fragment = PaymentFinishFragment.newInstance(PaymentFinishStatus.Successed)
            fragment.show(supportFragmentManager, "")
        } else {
            val fragment = PaymentFinishFragment.newInstance(PaymentFinishStatus.Failed, message, true)
            fragment.show(supportFragmentManager, "")
        }
    }

    override fun onSpeiSuccess(success: Boolean, message: String) {
        if (success) {
            val fragment = PaymentFinishFragment.newInstance(PaymentFinishStatus.Successed)
            fragment.show(supportFragmentManager, "")
        } else {
            val fragment = PaymentFinishFragment.newInstance(PaymentFinishStatus.Failed, message, true)
            fragment.show(supportFragmentManager, "")
        }
    }

    override fun onSpeiBackPressed() {
        showUi()
    }

    fun showPaymentOptions() {
        showUi()
    }
}