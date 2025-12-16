package ru.otus.basicarchitecture.ui.personalinfo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.otus.basicarchitecture.WizardCache
import javax.inject.Inject

@HiltViewModel
class PersonalInfoViewModel @Inject constructor(
    private val cache: WizardCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalInfoUiState())
    val uiState: StateFlow<PersonalInfoUiState> = _uiState

    init {
        // Вызываем валидацию при инициализации для установки начального состояния
        validate()
    }

    /**
     * Обработчик изменения имени
     */
    fun onFirstNameChange(value: String) {
        _uiState.value = _uiState.value.copy(firstName = value)
        validate()
    }

    /**
     * Обработчик изменения фамилии
     */
    fun onLastNameChange(value: String) {
        _uiState.value = _uiState.value.copy(lastName = value)
        validate()
    }

    /**
     * Обработчик изменения даты рождения
     */
    fun onBirthDateChange(value: String) {
        _uiState.value = _uiState.value.copy(birthDate = value)
        validate()
    }

    /**
     * Валидация введенных данных
     * Проверяет заполненность полей и корректность даты рождения
     */
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
        // isValid = true только если нет ошибки И дата либо пустая, либо полная и валидная
        val isValid = error == null && (state.birthDate.isBlank() || state.birthDate.length == 10)
        _uiState.value = _uiState.value.copy(error = error, isValid = isValid)
    }

    /**
     * Проверка, является ли пользователь совершеннолетним
     */
    fun isAdult(birthDate: String): Boolean {
        return DateValidator.isAdult(birthDate)
    }

    /**
     * Сохранение данных в кеш и переход к следующему экрану
     */
    fun saveAndProceed() {
        val state = _uiState.value
        cache.firstName = state.firstName
        cache.lastName = state.lastName
        cache.birthDate = state.birthDate
    }
}

data class PersonalInfoUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val error: String? = null,
    val isValid: Boolean = false
)

