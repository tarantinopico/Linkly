package com.example.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BackupRestoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val backupRestoreManager: BackupRestoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading("Exportování dat...")
            val result = backupRestoreManager.exportData(uri)
            if (result.isSuccess) {
                _uiState.value = SettingsUiState.Success("Data byla úspěšně zálohována.")
            } else {
                _uiState.value = SettingsUiState.Error("Nastala chyba při zálohování.")
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading("Obnovování dat...")
            val result = backupRestoreManager.importData(uri)
            if (result.isSuccess) {
                _uiState.value = SettingsUiState.Success("Data byla úspěšně obnovena.")
            } else {
                _uiState.value = SettingsUiState.Error("Nastala chyba. Soubor může být poškozen.")
            }
        }
    }

    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }

    class Factory(private val backupRestoreManager: BackupRestoreManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(backupRestoreManager) as T
        }
    }
}

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    data class Loading(val message: String) : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
