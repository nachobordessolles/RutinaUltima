package com.tuapp.rutina.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.rutina.data.ProgressStore
import com.tuapp.rutina.data.Progression
import com.tuapp.rutina.data.isComplete
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
    /** Series hechas por ejercicio, por día ("idProgresión/índiceDía"). Vive mientras la app esté abierta. */
    val sets: Map<String, List<Int>> = emptyMap(),
    /** Días completados esta semana ("idProgresión/índiceDía"). */
    val completedDays: Set<String> = emptySet(),
)

class RutinaViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RutinaRepository(app)
    private val progress = ProgressStore(app)

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
                    completedDays = progress.doneThisWeek(),
                )
            }
        }
    }

    /** Recalcula los días hechos (por si empezó una semana nueva con la app abierta). */
    fun refreshCompletedDays() {
        _state.update { it.copy(completedDays = progress.doneThisWeek()) }
    }

    fun setsFor(p: Int, d: Int): List<Int> {
        val progression = _state.value.progressions.getOrNull(p) ?: return emptyList()
        return _state.value.sets[ProgressStore.key(progression, d)].orEmpty()
    }

    /** Guarda las series del día y marca/desmarca el día como completado. */
    fun updateSets(p: Int, d: Int, setsDone: List<Int>) {
        val progression = _state.value.progressions.getOrNull(p) ?: return
        val day = progression.days.getOrNull(d) ?: return
        val key = ProgressStore.key(progression, d)
        val complete = day.isComplete(setsDone)
        val wasMarked = key in _state.value.completedDays
        // Solo desmarcar si se deshizo una serie de un día que estaba completo en esta sesión.
        val wasCompleteNow = day.isComplete(_state.value.sets[key].orEmpty())
        when {
            complete && !wasMarked -> progress.markDone(key)
            !complete && wasMarked && wasCompleteNow -> progress.unmark(key)
        }
        _state.update {
            it.copy(
                sets = it.sets + (key to setsDone),
                completedDays = progress.doneThisWeek(),
            )
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
