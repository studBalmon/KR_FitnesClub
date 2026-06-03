package com.example.fitnessapp.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.domain.model.CoachType
import com.example.fitnessapp.domain.model.WorkoutItem
import com.example.fitnessapp.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCatalogsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _coachTypes = MutableStateFlow<List<CoachType>>(emptyList())
    val coachTypes: StateFlow<List<CoachType>> = _coachTypes

    private val _coachTypesLoading = MutableStateFlow(true)
    val coachTypesLoading: StateFlow<Boolean> = _coachTypesLoading

    private val _workouts = MutableStateFlow<List<WorkoutItem>>(emptyList())
    val workouts: StateFlow<List<WorkoutItem>> = _workouts

    private val _workoutsLoading = MutableStateFlow(true)
    val workoutsLoading: StateFlow<Boolean> = _workoutsLoading

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch { loadCoachTypes() }
        viewModelScope.launch { loadWorkouts() }
    }

    private suspend fun loadCoachTypes() {
        _coachTypesLoading.value = true
        adminRepository.getCoachTypes()
            .onSuccess { _coachTypes.value = it }
            .onFailure { _snackbarMessage.value = "Ошибка загрузки типов тренеров: ${it.message}" }
        _coachTypesLoading.value = false
    }

    fun createCoachType(name: String) {
        viewModelScope.launch {
            adminRepository.createCoachType(name)
                .onSuccess { _snackbarMessage.value = "Тип тренера добавлен"; loadCoachTypes() }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка" }
        }
    }

    fun updateCoachType(id: Int, name: String) {
        viewModelScope.launch {
            adminRepository.updateCoachType(id, name)
                .onSuccess { _snackbarMessage.value = "Обновлено"; loadCoachTypes() }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка" }
        }
    }

    fun deleteCoachType(id: Int) {
        viewModelScope.launch {
            adminRepository.deleteCoachType(id)
                .onSuccess { _snackbarMessage.value = "Тип тренера удалён"; loadCoachTypes() }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка удаления" }
        }
    }

    private suspend fun loadWorkouts() {
        _workoutsLoading.value = true
        adminRepository.getWorkouts()
            .onSuccess { _workouts.value = it }
            .onFailure { _snackbarMessage.value = "Ошибка загрузки типов занятий: ${it.message}" }
        _workoutsLoading.value = false
    }

    fun createWorkout(name: String, description: String?, duration: Int, coachTypeId: Int) {
        viewModelScope.launch {
            adminRepository.createWorkout(name, description, duration, coachTypeId)
                .onSuccess { _snackbarMessage.value = "Тип занятия добавлен"; loadWorkouts() }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка" }
        }
    }

    fun updateWorkout(
        id: Int,
        name: String,
        description: String?,
        duration: Int,
        coachTypeId: Int
    ) {
        viewModelScope.launch {
            adminRepository.updateWorkout(id, name, description, duration, coachTypeId)
                .onSuccess { _snackbarMessage.value = "Обновлено"; loadWorkouts() }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка" }
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            adminRepository.deleteWorkout(id)
                .onSuccess { _snackbarMessage.value = "Тип занятия удалён"; loadWorkouts() }
                .onFailure { _snackbarMessage.value = it.message ?: "Ошибка удаления" }
        }
    }

    fun snackbarShown() {
        _snackbarMessage.value = null
    }
}
