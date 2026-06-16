package inc.tiptoppay.demo.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import inc.tiptoppay.demo.models.SdkConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "sdk_configs")

class ConfigurationRepository(private val context: Context) {

    private val configsKey = stringPreferencesKey("configurations")
    private val json = Json { ignoreUnknownKeys = true }

    val configurations: Flow<List<SdkConfiguration>> = context.dataStore.data.map { prefs ->
        prefs[configsKey]?.let { runCatching { json.decodeFromString<List<SdkConfiguration>>(it) }.getOrNull() }
            ?: emptyList()
    }

    suspend fun saveConfigurations(configs: List<SdkConfiguration>) {
        context.dataStore.edit { prefs ->
            prefs[configsKey] = json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(SdkConfiguration.serializer()),
                configs
            )
        }
    }
}