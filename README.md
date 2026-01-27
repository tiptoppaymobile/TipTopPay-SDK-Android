[![](https://jitpack.io/v/com.gitlab.tiptoppay.mobile:tiptoppay-sdk-android.svg)](https://jitpack.io/#com.gitlab.tiptoppay.mobile:tiptoppay-sdk-android)

## TipTopPay SDK для Android 

TipTopPay SDK позволяет интегрировать сервис обработки платежей в ваше Android-приложение.

### Требования
Android v. 6.0 and younger (API level 23)

### Подключение
1. В build.gradle уровня проекта добавить репозиторий Jitpack

```
repositories {
    maven { url 'https://jitpack.io' }
}
```
2. В build.gradle уровня приложения добавьте следующие зависимость указав последнюю доступную версию SDK:

[![](https://jitpack.io/v/tiptoppaymobile/TipTopPay-SDK-Android.svg)](https://jitpack.io/#tiptoppaymobile/TipTopPay-SDK-Android)

```
implementation 'com.github.tiptoppaymobile:TipTopPay-SDK-Android:latest-release'
```

### Возможности TipTopPay SDK:

Вы можете использовать SDK одним из трёх способов: 

* использовать стандартную платежную форму TipTopPay с уже готовым интерфейсом и коробочными решениями для оплаты
* реализовать платежную форму со своим визуалом с оплатой через готовые функции TipTopPayApi, которые позволяют оплатить любым из доступных методов
* реализовать платежную форму со своим визуалом с возможностью оплаты через ваш сервер. Что позволит помимо всего сохранять криптограммы на своем сервере для отложенной оплаты

### Структура проекта:

* **app** - Пример реализации приложения с использованием SDK
* **sdk** - Исходный код SDK


### Использование стандартной платежной формы TipTopPay SDK:

1.    Создайте TtpSdkLauncher  для получения результата через Activity Result API (рекомендуется использовать, но если хотите получить результат в onActivityResult этот шаг можно пропустить)

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

2. Создайте вспомогательные объекты и объект PaymentData, передайте через них всю необходимую информацию о платеже.

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

3. Создайте объект PaymentConfiguration, передайте в него Public Id из [личного кабинета](https://merchant.tiptoppay.kz/), объект PaymentData, а так же укажите другие параметры.

```
val configuration = PaymentConfiguration(
    publicId = publicId, // Your Public ID obtained in the Control Panel  
    paymentData = paymentData, // Payment data
    requireEmail = false, // Usage of email (false – not required, true – required)
    useDualMessagePayment = true, // Usage of two-staged payments (true). By default is using one-staged payments (false). Works only for Kazakhstan (Region.KZ)
    )
```

4. Вызовите форму оплаты. 

```
ttpSdkLauncher.launch(configuration) // Если используете Activity Result API

// или

TipTopPaySDK.getInstance().start(configuration, this, REQUEST_CODE_PAYMENT) // Если хотите получть результат в onActivityResult 
```

5. Получите результат в onActivityResult (если не используете Activity Result API)


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

#### Включение Google Pay в TipTopPay SDK

[Documentation](https://developers.google.com/payments/setup)


В build.gradle подключите следующую зависимость:

```
implementation 'com.google.android.gms:play-services-wallet:18.1.2'
```

Добавьте следующие meta data в манифест вашего приложения:

```
<meta-data
    android:name="com.google.android.gms.wallet.api.enabled"
    android:value="true" />
```

### Использование вашей платежной формы с использованием функций TipTopPayApi:

1. Создайте криптограмму карточных данных

**Для использования нового формата криптограммы:**

1.1. Получите **publicKey** и **keyVersion** в данном методе: [API](https://api.tiptoppay.kz/payments/publickey)

1.2. Используйте полученные **publicKey (Pem)**, **keyVersion (Version)**, а также **merchantPublicId** полученный в [[личном кабинете TipTopPay](https://merchant.tiptoppay.kz/) и данные карты для создания криптограммы

```
// Обязательно проверяйте входящие данные карты (номер, срок действия и cvc код) на корректность, иначе метод создания криптограммы вернет null
val cardCryptogram = Card.cardCryptogram(cardNumber, cardExpDate, cardCVC, merchantPublicId, publicKey, keyVersion)
```

2. Выполните запрос на проведения платежа (см. [[документацию по API](https://developers.tiptoppay.kz/#oplata-po-kriptogramme)).

3. Если необходимо, покажите 3DS форму для подтверждения платежа

```
val acsUrl = transaction.acsUrl
val paReq = transaction.paReq
val md = transaction.transactionId
ThreeDsDialogFragment
	.newInstance(acsUrl, paReq, md)
	.show(supportFragmentManager, "3DS")
```

4. Для получения формы 3DS и получения результатов прохождения 3DS аутентификации реализуйте протокол ThreeDSDialogListener. Передайте в запрос также threeDsCallbackId, полученный в ответ на auth или charge

```
override fun onAuthorizationCompleted(md: String, paRes: String) {
	// Используйте md и paRes, для завершения оплаты
}

override fun onAuthorizationFailed(error: String?) {
	Log.d("Error", "AuthorizationFailed: $error")
}
```

5. Выполните запрос post3ds для завершения оплаты (см. [документацию по API](https://developers.tiptoppay.kz/#obrabotka-3-d-secure)).

### Другие функции

* Проверка карточного номера на корректность

```
Card.isValidNumber(cardNumber)

```

* Проверка срока действия карты

```
Card.isValidExpDate(expDate) // expDate в формате MM/yy

```

* Определение типа платежной системы

```
let cardType: CardType = Card.cardType(from: cardNumberString)
```

* Шифрование cvv при оплате сохраненной картой и создание криптограммы для отправки на сервер

```
val cvvCryptogramPacket = Card.cardCryptogramForCVV(cvv)
```

### История обновлений:

#### 1.0.10
* Повышена стабильность работы, добавлено логирование

#### 1.0.2
* Добавлено сохранение карты

#### 1.0.1
* Добавлено объяснение некоторых причин отклонения платежей

#### 1.0.0
* Начальная версия

### Support

По возникающим вопросам технического характера обращайтесь на support-kz@tiptoppay.inc

