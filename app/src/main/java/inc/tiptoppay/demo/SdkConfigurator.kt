package inc.tiptoppay.demo

import inc.tiptoppay.demo.models.SdkParameter
import inc.tiptoppay.demo.models.getBool
import inc.tiptoppay.demo.models.getString
import inc.tiptoppay.demo.support.CardIOScanner
import inc.tiptoppay.sdk.api.models.PaymentDataPayer
import inc.tiptoppay.sdk.api.models.PaymentDataReceipt
import inc.tiptoppay.sdk.api.models.PaymentDataReceiptAmounts
import inc.tiptoppay.sdk.api.models.PaymentDataReceiptItem
import inc.tiptoppay.sdk.api.models.PaymentDataRecurrent
import inc.tiptoppay.sdk.configuration.PaymentConfiguration
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.models.Currency

object SdkConfigurator {

    fun buildDefaultParameters(): List<SdkParameter>{
        val defaultParameters = listOf(
            SdkParameter.BoolParam("dual_message_payment", "Dual message payment", false),
            SdkParameter.BoolParam("add_recurrent", "Add recurrent", false),
            SdkParameter.BoolParam("add_receipt", "Add receipt", false),
            SdkParameter.StringParam("public_id", "Public ID", "test_api_00000000000000000000002"),
            SdkParameter.StringParam("amount", "Amount", "10"),
            SdkParameter.StringParam("invoice_id", "InvoiceId (Optional)", "AB1234"),
            SdkParameter.StringParam("description", "Description (Optional)", "A basket of oranges"),
            SdkParameter.StringParam("account_id", "AccountId (Optional)", "AB12"),
            SdkParameter.StringParam("email", "Email (Optional)", "test@tiptoppay.inc"),
            SdkParameter.StringParam("json_data", "JsonData (Optional)", "{name: Alex}")
        )

        return defaultParameters
    }

    fun buildConfiguration(parameters: List<SdkParameter>): PaymentConfiguration {
        val params = parameters.associateBy { it.key }

        val isDualMessagePayment = params.getBool("dual_message_payment", false)
        val isAddRecurrent = params.getBool("add_recurrent", false)
        val isAddReceipt = params.getBool("add_receipt", false)

        val publicId = params.getString("public_id")
        val amount = params.getString("amount")
        val invoiceId = params.getString("invoice_id")
        val description = params.getString("description")
        val accountId = params.getString("account_id")
        val email = params.getString("email")
        val jsonData = params.getString("json_data")

        val payerFirstName = "Alex"
        val payerLastName = "Smith"
        val payerMiddleName = "-"
        val payerBirthDay = "1955-02-24"
        val payerAddress = "home 8, room 36"
        val payerStreet = "Baker Street"
        val payerCity = "London"
        val payerCountry = "EN"
        val payerPhone = "+39991234567"
        val payerPostcode = "123456"

        val payer = PaymentDataPayer().apply {
            firstName = payerFirstName
            lastName = payerLastName
            middleName = payerMiddleName
            birthDay = payerBirthDay
            address = payerAddress
            street = payerStreet
            city = payerCity
            country = payerCountry
            phone = payerPhone
            postcode = payerPostcode
        }

        val receiptItem = PaymentDataReceiptItem(
            label = description,
            price = 300.0,
            quantity = 3.0,
            amount = 900.0,
            vat = 20,
            method = 0,
            objectt = 0
        )
        val receiptItems = arrayListOf(receiptItem)

        val receiptAmounts = PaymentDataReceiptAmounts(
            electronic = 900.0,
            advancePayment = 0.0,
            credit = 0.0,
            provision = 0.0
        )

        val receipt = PaymentDataReceipt(
            items = receiptItems,
            taxationSystem = 0,
            email = email,
            phone = payerPhone,
            isBso = false,
            amounts = receiptAmounts
        )

        val recurrent = PaymentDataRecurrent(
            interval = "Month",
            period = 1,
            customerReceipt = receipt
        )

        val paymentData = PaymentData(
            amount = amount,
            currency = Currency.KZT,
            invoiceId = invoiceId,
            description = description,
            accountId = accountId,
            email = email,
            payer = payer,
            receipt = if (isAddReceipt) receipt else null,
            recurrent = if (isAddRecurrent) recurrent else null,
            jsonData = jsonData
        )

        return PaymentConfiguration(
            publicId = publicId,
            paymentData = paymentData,
            scanner = CardIOScanner(),
            requireEmail = false,
            useDualMessagePayment = isDualMessagePayment,
            testMode = true
        )
    }
}