package inc.tiptoppay.demo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import inc.tiptoppay.demo.SdkConfigurator
import inc.tiptoppay.demo.data.ConfigurationRepository
import inc.tiptoppay.demo.models.SdkConfiguration
import inc.tiptoppay.demo.models.SdkParameter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ConfigurationRepository(app)

    private val defaultParameters = SdkConfigurator.buildDefaultParameters()

    private val _parameters = MutableStateFlow(defaultParameters)
    val parameters: StateFlow<List<SdkParameter>> = _parameters.asStateFlow()

    private val _configurations = MutableStateFlow<List<SdkConfiguration>>(emptyList())
    val configurations: StateFlow<List<SdkConfiguration>> = _configurations.asStateFlow()

    init {
        viewModelScope.launch {
            repository.configurations.collect { _configurations.value = it }
        }
    }

    fun updateParameter(updated: SdkParameter) {
        _parameters.value = _parameters.value.map {
            if (it.key == updated.key) updated else it
        }
    }

    fun resetToDefaults() {
        _parameters.value = defaultParameters
    }

    fun saveConfiguration(name: String) {
        viewModelScope.launch {
            val newConfig = SdkConfiguration(name, _parameters.value)
            val existing = _configurations.value.filterNot { it.name == name }
            repository.saveConfigurations(existing + newConfig)
        }
    }

    fun loadConfiguration(config: SdkConfiguration) {
        val loadedMap = config.parameters.associateBy { it.key }
        _parameters.value = defaultParameters.map { default ->
            loadedMap[default.key] ?: default
        }
    }

    fun deleteConfiguration(config: SdkConfiguration) {
        viewModelScope.launch {
            repository.saveConfigurations(_configurations.value.filterNot { it.name == config.name })
        }
    }

    /**
     * Возвращает текущие параметры в виде Map для удобного доступа по ключу
     */
    fun getParametersMap(): Map<String, SdkParameter> {
        return _parameters.value.associateBy { it.key }
    }
}