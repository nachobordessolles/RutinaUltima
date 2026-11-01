package com.tuapp.rutina.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.rutina.data.Progression
import com.tuapp.rutina.data.RutinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Loading : UpdateStatus
    data object Success : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

data class RutinaUiState(
    val progressions: List<Progression> = emptyList(),
    val loading: Boolean = true,
    val loadError: String? = null,
    val lastUpdate: Long? = null,
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
)

class RutinaViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RutinaRepository(app)

    private val _state = MutableStateFlow(RutinaUiState())
    val state: StateFlow<RutinaUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val result = runCatching { repository.load() }
            _state.update {
                it.copy(
                    progressions = result.getOrDefault(emptyList()),
                    loading = false,
                    loadError = result.exceptionOrNull()?.let { e -> "No se pudo leer la rutina: ${e.message}" },
                    lastUpdate = repository.lastUpdate(),
                )
            }
        }
    }

    fun updateRoutine() {
        if (_state.value.updateStatus == UpdateStatus.Loading) return
        _state.update { it.copy(updateStatus = UpdateStatus.Loading) }
        viewModelScope.launch {
            repository.update()
                .onSuccess { list ->
                    _state.update {
                        it.copy(
                            progressions = list,
                            loadError = null,
                            lastUpdate = repository.lastUpdate(),
                            updateStatus = UpdateStatus.Success,
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(updateStatus = UpdateStatus.Error(e.message ?: e.javaClass.simpleName))
                    }
                }
        }
    }
}
