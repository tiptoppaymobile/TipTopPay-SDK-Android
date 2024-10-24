package inc.tiptoppay.sdk.util

import inc.tiptoppay.sdk.api.models.TipTopPayTransaction
import inc.tiptoppay.sdk.configuration.PaymentData
import inc.tiptoppay.sdk.ui.dialogs.PaymentFinishStatus
import inc.tiptoppay.sdk.viewmodel.PaymentCashViewModel
import inc.tiptoppay.sdk.viewmodel.PaymentCashViewModelFactory
import inc.tiptoppay.sdk.viewmodel.PaymentFinishViewModelFactory
import inc.tiptoppay.sdk.viewmodel.PaymentOptionsViewModelFactory
import inc.tiptoppay.sdk.viewmodel.PaymentProcessViewModelFactory

internal object InjectorUtils {

    fun providePaymentOptionsViewModelFactory(paymentData: PaymentData, useDualMessagePayment: Boolean): PaymentOptionsViewModelFactory {
        return PaymentOptionsViewModelFactory(paymentData, useDualMessagePayment)
    }

    fun providePaymentProcessViewModelFactory(paymentData: PaymentData, cryptogram: String, installmentsTerm: Int, useDualMessagePayment: Boolean, saveCard: Boolean?): PaymentProcessViewModelFactory {
        return PaymentProcessViewModelFactory(paymentData, cryptogram, installmentsTerm, useDualMessagePayment, saveCard)
    }

    fun providePaymentFinishViewModelFactory(status: PaymentFinishStatus,
                                             transaction: TipTopPayTransaction?,
                                             reasonCode: String?): PaymentFinishViewModelFactory {
        return PaymentFinishViewModelFactory(status,
                                             transaction,
                                             reasonCode)
    }

    fun providePaymentCashViewModelFactory(paymentData: PaymentData): PaymentCashViewModelFactory {
        return PaymentCashViewModelFactory(paymentData)
    }
}