package inc.tiptoppay.demo.screens.main

import android.os.Bundle
import android.widget.Toast
import inc.tiptoppay.demo.R
import inc.tiptoppay.demo.base.BaseActivity
import inc.tiptoppay.demo.databinding.ActivityMainBinding
import inc.tiptoppay.demo.support.CardIOScanner
import inc.tiptoppay.sdk.api.models.PaymentDataPayer
import inc.tiptoppay.sdk.configuration.TipTopPaySDK
import inc.tiptoppay.sdk.configuration.PaymentConfiguration
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.models.Currency
import inc.tiptoppay.sdk.models.Region

class MainActivity : BaseActivity() {

	private val ttpSdkLauncher = TipTopPaySDK.getInstance().launcher(this, result = {
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

	override val layoutId = R.layout.activity_main

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)

		binding.buttonRunTop.setOnClickListener {
			runTtpSdk()
		}

		binding.buttonRun.setOnClickListener {
			runTtpSdk()
		}
	}

	private fun runTtpSdk() {

		val apiUrl = binding.editApiUrl.text.toString()
		val publicId = binding.editPublicId.text.toString()
		val amount = binding.editAmount.text.toString()
		val invoiceId = binding.editInvoiceId.text.toString()
		val description = binding.editDescription.text.toString()
		val accountId = binding.editAccountId.text.toString()
		val email = binding.editEmail.text.toString()

		val payerFirstName = binding.editPayerFirstName.text.toString()
		val payerLastName = binding.editPayerLastName.text.toString()
		val payerMiddleName = binding.editPayerMiddleName.text.toString()
		val payerBirthDay = binding.editPayerBirth.text.toString()
		val payerAddress = binding.editPayerAddress.text.toString()
		val payerStreet = binding.editPayerStreet.text.toString()
		val payerCity = binding.editPayerCity.text.toString()
		val payerCountry = binding.editPayerCountry.text.toString()
		val payerPhone = binding.editPayerPhone.text.toString()
		val payerPostcode = binding.editPayerPostcode.text.toString()

		val jsonData = binding.editJsonData.text.toString()
		val isDualMessagePayment = binding.checkboxDualMessagePayment.isChecked

		var payer = PaymentDataPayer()
		payer.firstName = payerFirstName
		payer.lastName = payerLastName
		payer.middleName = payerMiddleName
		payer.birthDay = payerBirthDay
		payer.address = payerAddress
		payer.street = payerStreet
		payer.city = payerCity
		payer.country = payerCountry
		payer.phone = payerPhone
		payer.postcode = payerPostcode

		val paymentData = PaymentData(
			amount = amount,
			currency = Currency.MXN,
			invoiceId = invoiceId,
			description = description,
			accountId = accountId,
			email = email,
			payer = payer,
			jsonData = jsonData
		)

		val configuration = PaymentConfiguration(
			publicId = publicId,
			region = Region.MX,
			paymentData = paymentData,
			scanner = CardIOScanner(),
			requireEmail = false,
			useDualMessagePayment = isDualMessagePayment,
			apiUrl = apiUrl,
			testMode = true
		)
		ttpSdkLauncher.launch(configuration)
	}
}