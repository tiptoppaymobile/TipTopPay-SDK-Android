package inc.tiptoppay.sdk

class Constants {
	companion object {

//		const val ENVIRONMENT = "-qa-5"
//		const val ENVIRONMENT = "-preprod"
		const val ENVIRONMENT = ""

		const val baseApiUrl = "https://api$ENVIRONMENT.tiptoppay.kz/"

		const val fromtMonitoringApiUrl = "https://fm.tiptoppay.kz/"
		const val fromtMonitoringApiKey = "79646ac4-94ee-4acf-8a7a-3ff346629b52"
		const val disableFrontMonitoring = false

		const val userAgent = "Android SDK 1.0.0"
	}
}