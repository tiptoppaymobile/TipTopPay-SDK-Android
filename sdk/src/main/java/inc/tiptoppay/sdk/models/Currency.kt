package inc.tiptoppay.sdk.models

enum class Currency(val code: String, val symbol: String) {

	MXN("MXN", "MXN"),
	KZT( "KZT", "₸"),
	RUB("RUB", "\u20BD"),
	USD("USD", "$"),
	EUR("EUR", "€"),
	GBP("GBP", "£"),
	BYN("BYN", "Br"),
	UAH("UAH", "грн"),
	CHF("CHF", "Fr"),
	AZN("AZN", "man"),
	CZK("CZK", "Kč"),
	CAD("CAD", "C$"),
	PLN("PLN", "zł"),
	SEK("SEK", "kr"),
	TRY("TRY", "₺"),
	CNY("CNY", "CNY"),
	INR("INR", "र"),
	BRL("BRL", "R$"),
	ZAR("ZAR", "R")
}