package inc.tiptoppay.sdk.util

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.security.MessageDigest
import java.util.Locale


open class TextWatcherAdapter: TextWatcher {
	override fun afterTextChanged(s: Editable?) {}

	override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}

	override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {}
}

fun payerNameIsValid(name: String?): Boolean {
	if (name.isNullOrBlank()) {
		return false
	}
	return true
}

fun emailIsValid(email: String?): Boolean {
	if (email.isNullOrBlank()) {
		return false
	}
	return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun checkAndGetCorrectJsonDataString(json: String?): String? {
	return try {
		val parser = JsonParser()
		val jsonElement = parser.parse(json)
		Gson().toJson(jsonElement)
	} catch (e: JsonSyntaxException) {
		Log.e("TipTopPaySDK", "JsonSyntaxException in JsonData")
		null
	} catch (e: NullPointerException) {
		Log.e("TipTopPaySDK", "NullPointerException in JsonData")
		null
	}
}

fun getRussianLocale() = Locale("ru", "RU")

fun getSha512(input: String): String {
	return MessageDigest.getInstance("SHA-512")
		.digest(input.toByteArray())
		.joinToString(separator = "") {
			((it.toInt() and 0xff) + 0x100)
				.toString(16)
				.substring(1)
		}
}

fun getCurrentLocale(context: Context): Locale {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		context.resources.configuration.locales[0]
	} else {
		context.resources.configuration.locale
	}
}
