package inc.tiptoppay.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import inc.tiptoppay.demo.models.SdkConfiguration
import inc.tiptoppay.demo.models.SdkParameter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onRunSdk: (List<SdkParameter>) -> Unit = {}
) {
    val parameters by viewModel.parameters.collectAsState()
    val configurations by viewModel.configurations.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("TTP SDK Demo") },
                actions = {
                    IconButton(onClick = { viewModel.resetToDefaults() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Сбросить")
                    }
                    IconButton(onClick = { showLoadDialog = true }) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = "Загрузить")
                    }
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Filled.Save, contentDescription = "Сохранить")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onRunSdk(parameters) },
                icon = {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                },
                text = { Text("Запустить SDK") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp // отступ под FAB
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = parameters, key = { it.key }) { param ->
                ParameterItem(
                    param = param,
                    onUpdate = { viewModel.updateParameter(it) }
                )
            }
        }
    }

    if (showSaveDialog) {
        SaveConfigDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.saveConfiguration(name)
                showSaveDialog = false
            }
        )
    }

    if (showLoadDialog) {
        LoadConfigDialog(
            configurations = configurations,
            onDismiss = { showLoadDialog = false },
            onLoad = {
                viewModel.loadConfiguration(it)
                showLoadDialog = false
            },
            onDelete = { viewModel.deleteConfiguration(it) }
        )
    }
}

@Composable
fun ParameterItem(
    param: SdkParameter,
    onUpdate: (SdkParameter) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = param.label, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = param.key,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.outline
//            )
//            Spacer(modifier = Modifier.height(8.dp))

            when (param) {
                is SdkParameter.BoolParam -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = param.value,
                            onCheckedChange = { onUpdate(param.copy(value = it)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (param.value) "Включено" else "Выключено")
                    }
                }
                is SdkParameter.StringParam -> {
                    val isMultiline = param.key == "json_data"
                    OutlinedTextField(
                        value = param.value,
                        onValueChange = { onUpdate(param.copy(value = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = !isMultiline,
                        minLines = if (isMultiline) 3 else 1,
                        maxLines = if (isMultiline) 6 else 1
                    )
                }
                is SdkParameter.IntParam -> {
                    OutlinedTextField(
                        value = param.value.toString(),
                        onValueChange = { txt ->
                            val v = txt.filter { it.isDigit() }.toIntOrNull() ?: 0
                            onUpdate(param.copy(value = v))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                is SdkParameter.EnumParam -> {
                    EnumDropdown(param = param, onUpdate = onUpdate)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnumDropdown(
    param: SdkParameter.EnumParam,
    onUpdate: (SdkParameter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = param.value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            param.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onUpdate(param.copy(value = option))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SaveConfigDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Сохранить конфигурацию")
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) onSave(name.trim())
                },
                enabled = name.isNotBlank()
            ) {
                Text(text = "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена")
            }
        }
    )
}

@Composable
fun LoadConfigDialog(
    configurations: List<SdkConfiguration>,
    onDismiss: () -> Unit,
    onLoad: (SdkConfiguration) -> Unit,
    onDelete: (SdkConfiguration) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Загрузить конфигурацию")
        },
        text = {
            if (configurations.isEmpty()) {
                Text(text = "Нет сохранённых конфигураций")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items = configurations, key = { it.name }) { config ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { onLoad(config) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = config.name,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            IconButton(onClick = { onDelete(config) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Удалить"
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Закрыть")
            }
        }
    )
}