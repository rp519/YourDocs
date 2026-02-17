package com.yourdocs.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourdocs.data.backup.BackupManager
import com.yourdocs.data.billing.BillingEvent
import com.yourdocs.data.billing.BillingManager
import com.yourdocs.data.billing.PremiumRepository
import com.yourdocs.data.preferences.SortOrder
import com.yourdocs.data.preferences.UserPreferencesRepository
import com.yourdocs.domain.usecase.SetupPinUseCase
import com.yourdocs.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface BackupState {
    data object Idle : BackupState
    data class InProgress(val progress: Float) : BackupState
    data class Complete(val file: File) : BackupState
    data class Error(val message: String) : BackupState
}

sealed interface RestoreState {
    data object Idle : RestoreState
    data class InProgress(val progress: Float) : RestoreState
    data class Complete(val foldersRestored: Int, val documentsRestored: Int) : RestoreState
    data class Error(val message: String) : RestoreState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val setupPinUseCase: SetupPinUseCase,
    premiumRepository: PremiumRepository,
    val billingManager: BillingManager
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val biometricEnabled: StateFlow<Boolean> = preferencesRepository.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val sortOrder: StateFlow<SortOrder> = preferencesRepository.sortOrder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOrder.DATE_NEWEST)

    val isPinConfigured: StateFlow<Boolean> = preferencesRepository.isPinConfigured
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPremium: StateFlow<Boolean> = premiumRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val productPrice: StateFlow<String?> = billingManager.productDetails
        .map { it?.oneTimePurchaseOfferDetails?.formattedPrice }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val billingEvents: SharedFlow<BillingEvent> = billingManager.billingEvents

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            preferencesRepository.setSortOrder(order)
        }
    }

    fun setupPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            setupPinUseCase(pin)
                .onSuccess { onResult(true) }
                .onFailure { onResult(false) }
        }
    }

    fun removePin() {
        viewModelScope.launch {
            preferencesRepository.setPinHash(null)
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.InProgress(0f)
            try {
                val file = backupManager.createBackup { progress ->
                    _backupState.value = BackupState.InProgress(progress)
                }
                _backupState.value = BackupState.Complete(file)
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Backup failed")
            }
        }
    }

    fun restoreFromBackup(zipUri: Uri) {
        viewModelScope.launch {
            _restoreState.value = RestoreState.InProgress(0f)
            try {
                val result = backupManager.restoreFromBackup(zipUri) { progress ->
                    _restoreState.value = RestoreState.InProgress(progress)
                }
                _restoreState.value = RestoreState.Complete(result.foldersRestored, result.documentsRestored)
            } catch (e: Exception) {
                _restoreState.value = RestoreState.Error(e.message ?: "Restore failed")
            }
        }
    }

    fun restorePurchases(onResult: (Boolean) -> Unit) {
        billingManager.restorePurchases(onResult)
    }

    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }

    fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }
}
