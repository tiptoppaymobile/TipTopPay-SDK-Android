package inc.tiptoppay.sdk.models

import android.content.Context
import inc.tiptoppay.sdk.R

class ApiError {

	companion object {

		val CODE_ERROR_CONNECTION = "error_connection"
		val UNIVERSAL_ERROR_CODE = "5204"

		fun getFullErrorDescription(context: Context, code: String): String {
			var error = getErrorDescription(context, code)
			var errorExtra = getErrorDescriptionExtra(context, code)
			if (error.isEmpty()) {
				error = getErrorDescription(context, UNIVERSAL_ERROR_CODE)
				errorExtra = getErrorDescriptionExtra(context, UNIVERSAL_ERROR_CODE)
			}
			return "$error. $errorExtra"
		}

		fun getErrorDescription(context: Context, code: String): String {
			return when(code) {
				"3001" -> context.getString(R.string.ttpsdk_error_3001)
				"3002" -> context.getString(R.string.ttpsdk_error_3002)
				"3003" -> context.getString(R.string.ttpsdk_error_3003)
				"3004" -> context.getString(R.string.ttpsdk_error_3004)
				"3005" -> context.getString(R.string.ttpsdk_error_3005)
				"3006" -> context.getString(R.string.ttpsdk_error_3006)
				"3007" -> context.getString(R.string.ttpsdk_error_3007)
				"3008" -> context.getString(R.string.ttpsdk_error_3008)
				"5001" -> context.getString(R.string.ttpsdk_error_5001)
				"5003" -> context.getString(R.string.ttpsdk_error_5003)
				"5004" -> context.getString(R.string.ttpsdk_error_5004)
				"5005" -> context.getString(R.string.ttpsdk_error_5005)
				"5006" -> context.getString(R.string.ttpsdk_error_5006)
				"5007" -> context.getString(R.string.ttpsdk_error_5007)
				"5012" -> context.getString(R.string.ttpsdk_error_5012)
				"5013" -> context.getString(R.string.ttpsdk_error_5013)
				"5014" -> context.getString(R.string.ttpsdk_error_5014)
				"5015" -> context.getString(R.string.ttpsdk_error_5015)
				"5019" -> context.getString(R.string.ttpsdk_error_5019)
				"5030" -> context.getString(R.string.ttpsdk_error_5030)
				"5031" -> context.getString(R.string.ttpsdk_error_5031)
				"5034" -> context.getString(R.string.ttpsdk_error_5034)
				"5036" -> context.getString(R.string.ttpsdk_error_5036)
				"5041" -> context.getString(R.string.ttpsdk_error_5041)
				"5043" -> context.getString(R.string.ttpsdk_error_5043)
				"5051" -> context.getString(R.string.ttpsdk_error_5051)
				"5054" -> context.getString(R.string.ttpsdk_error_5054)
				"5057" -> context.getString(R.string.ttpsdk_error_5057)
				"5059" -> context.getString(R.string.ttpsdk_error_5059)
				"5061" -> context.getString(R.string.ttpsdk_error_5061)
				"5062" -> context.getString(R.string.ttpsdk_error_5062)
				"5063" -> context.getString(R.string.ttpsdk_error_5063)
				"5065" -> context.getString(R.string.ttpsdk_error_5065)
				"5082" -> context.getString(R.string.ttpsdk_error_5082)
				"5091" -> context.getString(R.string.ttpsdk_error_5091)
				"5092" -> context.getString(R.string.ttpsdk_error_5092)
				"5096" -> context.getString(R.string.ttpsdk_error_5096)
				"5204" -> context.getString(R.string.ttpsdk_error_5204)
				"5206" -> context.getString(R.string.ttpsdk_error_5206)
				"5207" -> context.getString(R.string.ttpsdk_error_5207)
				"5300" -> context.getString(R.string.ttpsdk_error_5300)
				CODE_ERROR_CONNECTION -> context.getString(R.string.ttpsdk_error_connection)
				else -> context.getString(R.string.ttpsdk_error_5204)
			}
		}

		fun getErrorDescriptionExtra(context: Context, code: String): String {
			return when(code) {
				"3001" -> context.getString(R.string.ttpsdk_error_3001_extra)
				"3002" -> context.getString(R.string.ttpsdk_error_3002_extra)
				"3003" -> context.getString(R.string.ttpsdk_error_3003_extra)
				"3004" -> context.getString(R.string.ttpsdk_error_3004_extra)
				"3005" -> context.getString(R.string.ttpsdk_error_3005_extra)
				"3006" -> context.getString(R.string.ttpsdk_error_3006_extra)
				"3007" -> context.getString(R.string.ttpsdk_error_3007_extra)
				"3008" -> context.getString(R.string.ttpsdk_error_3008_extra)
				"5001" -> context.getString(R.string.ttpsdk_error_5001_extra)
				"5003" -> context.getString(R.string.ttpsdk_error_5003_extra)
				"5004" -> context.getString(R.string.ttpsdk_error_5004_extra)
				"5005" -> context.getString(R.string.ttpsdk_error_5005_extra)
				"5006" -> context.getString(R.string.ttpsdk_error_5006_extra)
				"5007" -> context.getString(R.string.ttpsdk_error_5007_extra)
				"5012" -> context.getString(R.string.ttpsdk_error_5012_extra)
				"5013" -> context.getString(R.string.ttpsdk_error_5013_extra)
				"5014" -> context.getString(R.string.ttpsdk_error_5014_extra)
				"5015" -> context.getString(R.string.ttpsdk_error_5015_extra)
				"5019" -> context.getString(R.string.ttpsdk_error_5019_extra)
				"5030" -> context.getString(R.string.ttpsdk_error_5030_extra)
				"5031" -> context.getString(R.string.ttpsdk_error_5031_extra)
				"5034" -> context.getString(R.string.ttpsdk_error_5034_extra)
				"5036" -> context.getString(R.string.ttpsdk_error_5036_extra)
				"5041" -> context.getString(R.string.ttpsdk_error_5041_extra)
				"5043" -> context.getString(R.string.ttpsdk_error_5043_extra)
				"5051" -> context.getString(R.string.ttpsdk_error_5051_extra)
				"5054" -> context.getString(R.string.ttpsdk_error_5054_extra)
				"5057" -> context.getString(R.string.ttpsdk_error_5057_extra)
				"5059" -> context.getString(R.string.ttpsdk_error_5059_extra)
				"5061" -> context.getString(R.string.ttpsdk_error_5061_extra)
				"5062" -> context.getString(R.string.ttpsdk_error_5062_extra)
				"5063" -> context.getString(R.string.ttpsdk_error_5063_extra)
				"5065" -> context.getString(R.string.ttpsdk_error_5065_extra)
				"5082" -> context.getString(R.string.ttpsdk_error_5082_extra)
				"5091" -> context.getString(R.string.ttpsdk_error_5091_extra)
				"5092" -> context.getString(R.string.ttpsdk_error_5092_extra)
				"5096" -> context.getString(R.string.ttpsdk_error_5096_extra)
				"5204" -> context.getString(R.string.ttpsdk_error_5204_extra)
				"5206" -> context.getString(R.string.ttpsdk_error_5206_extra)
				"5207" -> context.getString(R.string.ttpsdk_error_5207_extra)
				"5300" -> context.getString(R.string.ttpsdk_error_5300_extra)
				CODE_ERROR_CONNECTION -> context.getString(R.string.ttpsdk_error_connection_extra)
				else -> context.getString(R.string.ttpsdk_error_5204_extra)
			}
		}
	}
}