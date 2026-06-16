package inc.tiptoppay.demo.models

fun Map<String, SdkParameter>.getString(key: String, default: String = ""): String {
    return (this[key] as? SdkParameter.StringParam)?.value ?: default
}

fun Map<String, SdkParameter>.getBool(key: String, default: Boolean = false): Boolean {
    return (this[key] as? SdkParameter.BoolParam)?.value ?: default
}

fun Map<String, SdkParameter>.getInt(key: String, default: Int = 0): Int {
    return (this[key] as? SdkParameter.IntParam)?.value ?: default
}