package inc.tiptoppay.sdk.card

import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.google.gson.GsonBuilder
import inc.tiptoppay.sdk.api.models.CardCryptogramPacket
import inc.tiptoppay.sdk.api.models.CardInfo
import inc.tiptoppay.sdk.util.HexPacketHelper
import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class Card {
	companion object {

		@Deprecated("Use API: https://api.tiptoppay.kz/payments/publickey")
		private fun getKeyVersion(): String {
			return "04"
		}

		@Deprecated("Use API: https://api.tiptoppay.kz/payments/publickey")
		private fun getPublicKey(): String {
			return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArBZ1NNjvszen6BNWsgyDUJvDUZDtvR4jKNQtEwW1iW7hqJr0TdD8hgTxw3DfH+Hi/7ZjSNdH5EfChvgVW9wtTxrvUXCOyJndReq7qNMo94lHpoSIVW82dp4rcDB4kU+q+ekh5rj9Oj6EReCTuXr3foLLBVpH0/z1vtgcCfQzsLlGkSTwgLqASTUsuzfI8viVUbxE1a+600hN0uBh/CYKoMnCp/EhxV8g7eUmNsWjZyiUrV8AA/5DgZUCB+jqGQT/Dhc8e21tAkQ3qan/jQ5i/QYocA/4jW3WQAldMLj0PA36kINEbuDKq8qRh25v+k4qyjb7Xp4W2DywmNtG3Q20MQIDAQAB"
		}

		fun getType(number: String): CardType? {
			return CardType.fromString(number)
		}

		fun isValidNumber(cardNumber: String?): Boolean {
			if (cardNumber.isNullOrBlank()) {
				return false
			}

			val number = prepareCardNumber(cardNumber)
			if (TextUtils.isEmpty(number) || number.length < 14 || number.length > 19 ) {
				return false
			}

			// Luhn algorithm
			var checksum = 0

			for (i in number.length - 1 downTo 0 step 2) {
				checksum += number[i] - '0'
			}
			for (i in number.length - 2 downTo 0 step 2) {
				val n: Int = (number[i] - '0') * 2
				checksum += if (n > 9) n - 9 else n
			}

			return checksum % 10 == 0
		}

		fun isValidExpDate(exp: String?, skipExpiryValidation: Boolean?): Boolean {
			return if (skipExpiryValidation == null || skipExpiryValidation) {
				isValidExpDate(exp)
			} else {
				isValidExpDateFull(exp)
			}
		}

		fun isValidExpDate(exp: String?): Boolean {
			return if (exp == null) {
				false
			} else {
				val expDate = exp.replace("/", "")
				if (expDate.length != 4) {
					false
				} else {
					val month = expDate.substring(0,2).toInt()
					return month in 1..12
				}
			}
		}

		fun isValidExpDateFull(exp: String?): Boolean {
			return if (exp == null) {
				false
			} else {
				val expDate = exp.replace("/", "")
				if (expDate.length != 4) {
					false
				} else {
					val format: DateFormat = SimpleDateFormat("MMyy", Locale.ENGLISH)
					format.isLenient = false
					return try {
						var date = format.parse(expDate)
						val calendar = Calendar.getInstance()
						calendar.time = date ?: Date()
						calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
						date = calendar.time
						val currentDate = Date()
						currentDate.before(date)
					} catch (e: ParseException) {
						e.printStackTrace()
						false
					}
				}
			}
		}

		fun isValidCvv(cardNumber: String?, cvv: String?): Boolean {
			return if (cvv?.length!! in 3..4) {
				true
			} else isUzcardCard(cardNumber) || isHumoCard(cardNumber)
		}

		fun isUzcardCard(cardNumber: String?): Boolean {
			//Uzcard 8600
			if (cardNumber?.length!! > 3 && cardNumber?.substring(0, 4) == "8600") {
				return true
			}
			return false
		}

		fun isHumoCard(cardNumber: String?): Boolean {
			//Humo 9860
			if (cardNumber?.length!! > 3 && cardNumber?.substring(0, 4) == "9860") {
				return true
			}
			return false
		}

		private fun prepareCardNumber(cardNumber: String): String {
			return cardNumber.replace("\\s".toRegex(), "")
		}

		fun createHexPacketFromData(cardNumber: String, cardExp: String, cardCvv: String, publicId: String, publicKey: String, keyVersion: Int): String? {

			var clearPublicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")

			val clearNumber = prepareCardNumber(cardNumber)

			val cryptogram = createCardCryptogram(clearNumber, cardExp, cardCvv, publicId, clearPublicKey)

			val cardInfo = CardInfo(
				firstSixDigits = clearNumber.substring(0, 6),
				lastFourDigits = clearNumber.substring(clearNumber.length - 4),
				expDateMonth = cardExp.substring(0, 2),
				expDateYear = cardExp.substring(cardExp.length - 2)
			)


			var cardCryptogramPacket = CardCryptogramPacket(
				cardInfo = cardInfo,
				keyVersion = HexPacketHelper.numberToEvenLengthString(keyVersion),
				value = cryptogram
				)
			
			var gson = GsonBuilder().disableHtmlEscaping().create();
			var cardCryptogramPacketString = gson.toJson(cardCryptogramPacket)

			cardCryptogramPacketString = Base64.encodeToString(cardCryptogramPacketString.toByteArray(), Base64.NO_WRAP).trim()

			return cardCryptogramPacketString
		}
		@Throws(UnsupportedEncodingException::class, NoSuchPaddingException::class, NoSuchAlgorithmException::class, BadPaddingException::class,
				IllegalBlockSizeException::class, InvalidKeyException::class)
		fun createCardCryptogram(number: String, cardExp: String, cardCvv: String, publicId: String, publicKey: String): String? {
			val cardNumber = prepareCardNumber(number)
			var exp = cardExp.replace("/", "")
			if (cardNumber.length < 14 || exp.length != 4) {
				return null
			}

			exp = exp.substring(2, 4) + cardExp.substring(0, 2)
			val s = "$cardNumber@$exp@$cardCvv@$publicId"
			val bytes = s.toByteArray(charset("ASCII"))
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			val random = SecureRandom()

			cipher.init(Cipher.ENCRYPT_MODE, getRSAKey(publicKey), random)
			val crypto = cipher.doFinal(bytes)
			var crypto64 = Base64.encodeToString(crypto, Base64.DEFAULT)
			val cr_array = crypto64.split("\n").toTypedArray()
			crypto64 = ""
			for (i in cr_array.indices) {
				crypto64 += cr_array[i]
			}
			return crypto64
		}

		@Deprecated("Use API: https://api.tiptoppay.kz/payments/publickey to get publicKey and keyVersion, then use new method createHexPacketFromData(number, cardExp, cardCvv, publicId, publicKey, keyVersion)")
		@Throws(UnsupportedEncodingException::class, NoSuchPaddingException::class, NoSuchAlgorithmException::class, BadPaddingException::class,
				IllegalBlockSizeException::class, InvalidKeyException::class)
		fun cardCryptogram(number: String, cardExp: String, cardCvv: String, publicId: String): String? {
			val cardNumber = prepareCardNumber(number)
			var exp = cardExp.replace("/", "")
			if (cardNumber.length < 14 || exp.length != 4) {
				return null
			}
			val shortNumber =
				cardNumber.substring(0, 6) + cardNumber.substring(cardNumber.length - 4, cardNumber.length)

			exp = exp.substring(2, 4) + cardExp.substring(0, 2)
			val s = "$cardNumber@$exp@$cardCvv@$publicId"
			val bytes = s.toByteArray(charset("ASCII"))
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			val random = SecureRandom()
			cipher.init(Cipher.ENCRYPT_MODE, getRSAKey(getPublicKey()), random)
			val crypto = cipher.doFinal(bytes)
			var crypto64 = "01" +
					shortNumber +
					exp +
					getKeyVersion() +
					Base64.encodeToString(crypto, Base64.DEFAULT)
			val cr_array = crypto64.split("\n").toTypedArray()
			crypto64 = ""
			for (i in cr_array.indices) {
				crypto64 += cr_array[i]
			}
			return crypto64
		}

		@Throws(UnsupportedEncodingException::class, NoSuchPaddingException::class, NoSuchAlgorithmException::class, BadPaddingException::class, IllegalBlockSizeException::class, InvalidKeyException::class)
		fun cardCryptogramForCVV(cardCvv: String): String? {
			val bytes = cardCvv.toByteArray(charset("ASCII"))
			val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
			val random = SecureRandom()
			cipher.init(Cipher.ENCRYPT_MODE, getRSAKey(getPublicKey()), random)
			val crypto = cipher.doFinal(bytes)
			var crypto64 = "03" + getKeyVersion() + Base64.encodeToString(crypto, Base64.DEFAULT)
			val crArray = crypto64.split("\n").toTypedArray()
			crypto64 = ""
			for (i in crArray.indices) {
				crypto64 += crArray[i]
			}
			return crypto64
		}

		private fun getRSAKey(publicKey: String): PublicKey? {
			return try {
				val keyBytes: ByteArray =
					Base64.decode(publicKey.toByteArray(charset("utf-8")), Base64.DEFAULT)
				val spec = X509EncodedKeySpec(keyBytes)
				val kf: KeyFactory = KeyFactory.getInstance("RSA")
				kf.generatePublic(spec)
			} catch (e: NoSuchAlgorithmException) {
				e.printStackTrace()
				null
			} catch (e: InvalidKeySpecException) {
				e.printStackTrace()
				null
			} catch (e: UnsupportedEncodingException) {
				e.printStackTrace()
				null
			}
		}
	}
}