[![](https://jitpack.io/v/com.gitlab.tiptoppay.mobile:tiptoppay-sdk-android.svg)](https://jitpack.io/#com.gitlab.tiptoppay.mobile:tiptoppay-sdk-android)

## TipTopPay SDK for Android 

TipTopPay SDK позволяет интегрировать прием платежей в мобильные приложение для платформы Android.

### Требования
Для работы TipTopPay SDK необходим Android версии 6.0 или выше (API level 23)

### Подключение
В build.gradle уровня проекта добавить репозиторий Jitpack

```
repositories {
	maven { url 'https://jitpack.io' }
}
```
В build.gradle уровня приложения добавить зависимость указав последнюю доступную версию SDK:

[![](https://jitpack.io/v/com.github.tiptoppaymobile:TipTopPay-SDK-Android.svg)](https://jitpack.io/#com.github.tiptoppaymobile:TipTopPay-SDK-Android)

```
implementation 'com.github.tiptoppaymobile:TipTopPay-SDK-Android:latest-release'
```

### Структура проекта:

* **app** - Пример реализации приложения с использованием SDK
* **sdk** - Исходный код SDK


### Использование платежной формы SDK TipTopPay:

1.	Создайте TtpSdkLauncher для получения результата через Activity Result API (рекомендуется использовать, но если хотите получить результат в onActivityResult этот шаг можно пропустить)

```
val ttpSdkLauncher = TipTopPaySDK.getInstance().launcher(this, result = {
		if (it.status != null) {
			if (it.status == TipTopPaySDK.TransactionStatus.Succeeded) {
				Toast.makeText(this, "Успешно! Транзакция №${it.transactionId}", Toast.LENGTH_SHORT).show()
				finish()
			} else {
				if (it.reasonCode != 0) {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}. Код ошибки ${it.reasonCode}", Toast.LENGTH_SHORT).show()
				} else {
					Toast.makeText(this, "Ошибка! Транзакция №${it.transactionId}.", Toast.LENGTH_SHORT).show()
				}
			}
		}
	})
```

2. Создайте объект PaymentData, передайте в него сумму платежа, валюту и другие данные, если необходимо передать подробную информацию о плательщике создайте объект PaymentDataPayer с информацией о плательщике и также пердайте этот объект в PaymentData.

```
var payer = PaymentDataPayer() // Доп. поле, куда передается информация о плательщике:
payer.firstName = payerFirstName // Имя
payer.lastName = payerLastName // Фамилия
payer.middleName = payerMiddleName // Отчество
payer.birthDay = payerBirthDay // День рождения
payer.address = payerAddress // Адрес
payer.street = payerStreet // Улица
payer.city = payerCity // Город
payer.country = payerCountry // Страна
payer.phone = payerPhone // Телефон
payer.postcode = payerPostcode // Почтовый индекс

val paymentData = PaymentData(
	amount = amount, // Cумма платежа в валюте
	currency = currency, // Валюта
	invoiceId = invoiceId, // Номер счета или заказа
	description = description, // Описание оплаты в свободной форме
	accountId = accountId, // Идентификатор пользователя
	email = email, // E-mail плательщика, на который будет отправлена квитанция об оплате
	payer = payer, // Информация о плательщике
	jsonData = jsonData // Любые другие данные, которые будут связаны с транзакцией {name: Ivan}
)
```

3. Создайте объект PaymentConfiguration, передайте в него Public Id из [личного кабинета TipTopPay](https://merchant.tiptoppay.kz/), объект PaymentData, а так же укажите другие параметры.

```
val configuration = PaymentConfiguration(
	publicId = publicId, // Ваш PublicID в полученный в ЛК TipTopPay
	paymentData = paymentData, // Информация о платеже
	scanner = CardIOScanner(), // Сканер банковских карт
	requireEmail = false, // Обязателный email для проведения оплаты (по умолчанию false)
	useDualMessagePayment = true, // Использовать двухстадийную схему проведения платежа, по умолчанию используется одностадийная схема
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
					Toast.makeText(this, "Успешно! Транзакция №$transactionId", Toast.LENGTH_SHORT).show()
				} else {
					val reasonCode = data.getIntExtra(TipTopPaySDK.IntentKeys.TransactionReasonCode.name, 0) ?: 0
					if (reasonCode > 0) {
						Toast.makeText(this, "Ошибка! Транзакция №$transactionId. Код ошибки $reasonCode", Toast.LENGTH_SHORT).show()
					} else {
						Toast.makeText(this, "Ошибка! Транзакция №$transactionId.", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}
		else -> super.onActivityResult(requestCode, resultCode, data)
```

#### Подключение Google Pay  через TipTopPay

[Документация](https://developers.google.com/payments/setup)

#### Включение Google Pay 

В файл build.gradle подключите следующую зависимость:

```
implementation 'com.google.android.gms:play-services-wallet:18.1.2'
```

В файл манифест приложения добавьте мета информацию:

```
<meta-data
	android:name="com.google.android.gms.wallet.api.enabled"
	android:value="true" />
```
#### Проведение платежа через Google Pay с помощью формы TipTopPay

Никаких дополнительных шагов не требуется. Форма автоматически определяет, подключен Google Pay или нет. В зависимости от этого покажется форма выбора способа оплаты (Google Pay или карта) или форма ввода карточных данных

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

* Шифрование карточных данных и создание криптограммы для отправки на сервер

```
val cardCryptogram = Card.cardCryptogram(cardNumber, cardDate, cardCVC, Constants.MERCHANT_PUBLIC_ID)
```

* Шифрование cvv при оплате сохраненной картой и создание криптограммы для отправки на сервер

```
val cvvCryptogramPacket = Card.cardCryptogramForCVV(cvv)
```

* Отображение 3DS формы и получении результата 3DS аутентификации

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

* Сканер карт
Вы можете подключить любой сканер карт, который вызывается с помощью Activity. Для этого нужно реализовать протокол CardScanner и передать объект, реализующий протокол, при создании PaymentConfiguration. Если протокол не будет реализован, то кнопка сканирования не будет показана

Пример со сканером CardIO

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

### История обновлений:

#### 1.0.2
* Добавлено уведомление плательщика о сохранении карты

#### 1.0.1
* Добавлена расшифрока некоторых причин отказа в проведении платежа

#### 1.0.0
* Опубликована первая версия SDK

### Поддержка

По возникающим вопросам техничечкого характера обращайтесь на support-kz@tiptoppay.inc
