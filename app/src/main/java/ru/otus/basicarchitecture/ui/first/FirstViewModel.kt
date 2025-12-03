package ru.otus.basicarchitecture.ui.first

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.otus.basicarchitecture.WizardCache
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FirstViewModel @Inject constructor(
    private val cache: WizardCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(FirstUiState())
    val uiState: StateFlow<FirstUiState> = _uiState

    fun onFirstNameChange(value: String) {
        _uiState.value = _uiState.value.copy(firstName = value)
        validate()
    }

    fun onLastNameChange(value: String) {
        _uiState.value = _uiState.value.copy(lastName = value)
        validate()
    }

    fun onBirthDateChange(value: String) {
        _uiState.value = _uiState.value.copy(birthDate = value)
        validate()
    }

    private fun validate() {
        val state = _uiState.value
        val error = when {
            state.firstName.isBlank() -> "Введите имя"
            state.lastName.isBlank() -> "Введите фамилию"
            state.birthDate.isBlank() -> "Введите дату рождения"
            state.birthDate.length < 10 -> null // Не показываем ошибку если дата еще не введена полностью
            !isAdult(state.birthDate) -> "Возраст должен быть 18+"
            else -> null
        }
        _uiState.value = _uiState.value.copy(error = error, isValid = error == null)
    }

    fun isAdult(birthDate: String): Boolean {
        return try {
            // Проверяем что дата полная
            if (birthDate.length < 10) return true

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(birthDate) ?: return false

            val birthCalendar = Calendar.getInstance().apply { time = date }
            val currentCalendar = Calendar.getInstance()

            var age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            age >= 18
        } catch (e: Exception) {
            false
        }
    }

    fun saveAndProceed() {
        val state = _uiState.value
        cache.firstName = state.firstName
        cache.lastName = state.lastName
        cache.birthDate = state.birthDate
    }
}

data class FirstUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val error: String? = null,
    val isValid: Boolean = false
)
