package inc.tiptoppay.demo.models

import kotlinx.serialization.Serializable

@Serializable
sealed class SdkParameter {
    abstract val key: String
    abstract val label: String

    @Serializable
    data class BoolParam(
        override val key: String,
        override val label: String,
        val value: Boolean
    ) : SdkParameter()

    @Serializable
    data class StringParam(
        override val key: String,
        override val label: String,
        val value: String
    ) : SdkParameter()

    @Serializable
    data class IntParam(
        override val key: String,
        override val label: String,
        val value: Int
    ) : SdkParameter()

    @Serializable
    data class EnumParam(
        override val key: String,
        override val label: String,
        val value: String,
        val options: List<String>
    ) : SdkParameter()
}

@Serializable
data class SdkConfiguration(
    val name: String,
    val parameters: List<SdkParameter>
)