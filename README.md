[![](https://jitpack.io/v/com.gitlab.tiptoppay.mobile:tiptoppay-sdk-android.svg)](https://jitpack.io/#com.gitlab.tiptoppay.mobile:tiptoppay-sdk-android)

## TipTopPay SDK for Android 

TipTopPay SDK allows to integrate payment processing service into an Android application.

### Requirements
Android v. 6.0 and younger (API level 23)

### Connection
Add Jitpack repository into build.gradle at project level 

```
repositories {
    maven { url 'https://jitpack.io' }
}
```
Add dependence with indication of the latest SDK version into build.gradle at application level:

[![](https://jitpack.io/v/tiptoppaymobile/TipTopPay-SDK-Android.svg)](https://jitpack.io/#tiptoppaymobile/TipTopPay-SDK-Android)

```
implementation 'com.github.tiptoppaymobile:TipTopPay-SDK-Android:latest-release'
```

### Project structure:

* **app** - An example app using SDK
* **sdk** - Source code of SDK


### Payment form usage of TipTopPay SDK:

1.    Create TtpSdkLauncher to get result via Activity Result API (recommended, but this step can be skipped in case of getting result via onActivityResult)

```
val ttpSdkLauncher = TipTopPaySDK.getInstance().launcher(this, result = {
        if (it.status != null) {
            if (it.status == TipTopPaySDK.TransactionStatus.Succeeded) {
                Toast.makeText(this, " Succeeded! Transaction №${it.transactionId}", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                if (it.reasonCode != 0) {
                    Toast.makeText(this, "Error! Transaction №${it.transactionId}. Reason code ${it.reasonCode}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error! Transaction №${it.transactionId}.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    })
```

2. Create an object PaymentData, send into it payment amount, currency and other data. If it is necessary to send detailed customer’s data, create and use an object PaymentDataPayer, also sending it to PaymentData

```
var payer = PaymentDataPayer() // Extra parameter for customer’s data:
payer.firstName = payerFirstName // First name
payer.lastName = payerLastName // Last name
payer.middleName = payerMiddleName // Middle name
payer.birthDay = payerBirthDay // Birth date
payer.address = payerAddress // Address
payer.street = payerStreet // Street
payer.city = payerCity // City
payer.country = payerCountry // Country
payer.phone = payerPhone // Phone number
payer.postcode = payerPostcode // Postal code

val paymentData = PaymentData(
    amount = amount, // Payment amount
    currency = Currency.MXN, // Payment currency
    invoiceId = invoiceId, // Order or invoice number
    description = description, // Payment description
    accountId = accountId, // Customer’s identification number
    email = email, // Customer’s e-mail (used for sending payment confirmation)
    payer = payer, // Customer’s information
    jsonData = jsonData // Any other data linked to this payment {name: Ivan}
)
```

3. Create an object PaymentConfiguration, send into it Public Id obtained from [TipTopPay Control Panel](https://merchant.tiptoppay.kz/). Also send PaymentData and other parameters.

```
val configuration = PaymentConfiguration(
    publicId = publicId, // Your Public ID obtained in the Control Panel
    region = Region.MX, // Your region Mexico (MX) or Kazakhstan (KZ)    
    paymentData = paymentData, // Payment data
    scanner = CardIOScanner(), // Card scanner
    requireEmail = false, // Usage of email (false – not required, true – required)
    useDualMessagePayment = true, // Usage of two-staged payments (true). By default is using one-staged payments (false)
    )
```

4. Initiate the payment UI. 

```
ttpSdkLauncher.launch(configuration) // If you are using Activity Result API

// or

TipTopPaySDK.getInstance().start(configuration, this, REQUEST_CODE_PAYMENT) // In case of getting result via onActivityResult 
```

5. Get result via onActivityResult (if Activity Result API is not used)

```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when (requestCode) {
        REQUEST_CODE_PAYMENT -> {
            val transactionId = data?.getIntExtra(TipTopPaySDK.IntentKeys.TransactionId.name, 0) ?: 0
            val transactionStatus = data?.getSerializableExtra(TipTopPaySDK.IntentKeys.TransactionStatus.name) as? TipTopPaySDK.TransactionStatus


            if (transactionStatus != null) {
                if (transactionStatus == TipTopPaySDK.TransactionStatus.Succeeded) {
                    Toast.makeText(this, " Succeeded! Transaction №$transactionId", Toast.LENGTH_SHORT).show()
                } else {
                    val reasonCode = data.getIntExtra(TipTopPaySDK.IntentKeys.TransactionReasonCode.name, 0) ?: 0
                    if (reasonCode > 0) {
                        Toast.makeText(this, "Error! Transaction №$transactionId. Reason code $reasonCode", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error! Transaction №$transactionId.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else -> super.onActivityResult(requestCode, resultCode, data)
```

#### Google Pay  connection via TipTopPay

[Documentation](https://developers.google.com/payments/setup)

#### Enabling Google Pay 

In build.gradle connect the following dependence:

```
implementation 'com.google.android.gms:play-services-wallet:18.1.2'
```

Add meta data into the application manifest:

```
<meta-data
    android:name="com.google.android.gms.wallet.api.enabled"
    android:value="true" />
```
#### Google Pay payment acceptance using TipTopPay form

No additional steps are required. The form is determined automatically whether Google Pay is connected or not. Depending on this, the form for selecting the payment method (Google Pay or card) or the form for entering card data will be determined.

### Other functions

* Card number validation

```
Card.isValidNumber(cardNumber)

```

* Card expiry date validation

```
Card.isValidExpDate(expDate) // expDate in the format of MM/yy

```

* Payment system definition

```
let cardType: CardType = Card.cardType(from: cardNumberString)
```

* Card data encryption for sending to the server 

```
val cardCryptogram = Card.cardCryptogram(cardNumber, cardDate, cardCVC, Constants.MERCHANT_PUBLIC_ID)
```

* CVV encryption for payments by a saved card and sending to the server 

```
val cvvCryptogramPacket = Card.cardCryptogramForCVV(cvv)
```

* Displaying 3DS page and getting authentication result

```
val acsUrl = transaction.acsUrl
val paReq = transaction.paReq
val md = transaction.transactionId
ThreeDsDialogFragment
    .newInstance(acsUrl, paReq, md)
    .show(supportFragmentManager, "3DS")

interface ThreeDSDialogListener {
    fun onAuthorizationCompleted(md: String, paRes: String)
    fun onAuthorizationFailed(error: String?)
}
```

* Card scanner
You can connect any card scanner that is created using Activity. To do this, you need to implement the CardScanner protocol and pass an object implementing the protocol when creating PaymentConfiguration. If the protocol is not implemented, the enable button will not be shown.
An example using CardIO scanner

```
@Parcelize
class CardIOScanner: CardScanner() {
    override fun getScannerIntent(context: Context) =
        Intent(context, CardIOActivity::class.java).apply {
            putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
        }

    override fun getCardDataFromIntent(data: Intent) =
        if (data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            val scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT) as? CreditCard
            val month = (scanResult?.expiryMonth ?: 0).toString().padStart(2, '0')
            val yearString = scanResult?.expiryYear?.toString() ?: "00"
            val year = if (yearString.length > 2) {
                yearString.substring(yearString.lastIndex - 1)
            } else {
                yearString.padStart(2, '0')
            }
            val cardData = CardData(scanResult?.cardNumber, month, year, scanResult?.cardholderName)
            cardData
        } else {
            null
        }
}
```

### Update history:

#### 1.0.3
* Added region and localization


#### 1.0.2
* Added customer’s notification at saving a card

#### 1.0.1
* Added explanation of some reasons of payment declines
#### 1.0.0
* Initial version

### Support

Contact soporte@tiptoppay.inc (Mexico) or support-kz@tiptoppay.inc (Kazakhstan)

