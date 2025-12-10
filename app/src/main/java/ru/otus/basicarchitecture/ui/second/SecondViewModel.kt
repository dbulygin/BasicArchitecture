package ru.otus.basicarchitecture.ui.second

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.otus.basicarchitecture.WizardCache
import ru.otus.basicarchitecture.network.dadata.DadataApi
import ru.otus.basicarchitecture.network.dadata.DadataRequest
import javax.inject.Inject

@HiltViewModel
class SecondViewModel @Inject constructor(
    private val cache: WizardCache,
    private val dadataApi: DadataApi
) : ViewModel() {

    // Состояние для хранения списка подсказок адресов
    private val _addressSuggestions = MutableStateFlow<List<String>>(emptyList())
    val addressSuggestions: StateFlow<List<String>> = _addressSuggestions

    // Job для отмены предыдущего запроса при новом вводе
    private var searchJob: Job? = null

    /**
     * Получение подсказок адресов по введенному тексту
     * Использует debounce для уменьшения количества запросов
     */
    fun getAddressSuggestions(query: String) {
        // Отменяем предыдущий запрос, если он еще выполняется
        searchJob?.cancel()

        // Если запрос слишком короткий, очищаем подсказки
        if (query.trim().length < 3) {
            _addressSuggestions.value = emptyList()
            return
        }

        // Запускаем новый запрос с задержкой (debounce)
        val currentQuery = query.trim()
        searchJob = viewModelScope.launch {
            try {
                delay(500) // Задержка 300мс для debounce

                // Выполняем запрос к API Дадата
                val response = dadataApi.getAddressSuggestions(
                    DadataRequest(query = currentQuery, count = 10)
                )

                // Извлекаем значения адресов из ответа
                // Если корутина была отменена, будет выброшена CancellationException
                val suggestions = response.suggestions.map { it.value }
                _addressSuggestions.value = suggestions
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Игнорируем отмену при новом вводе
                throw e
            } catch (e: Exception) {
                // В случае ошибки очищаем подсказки
                // Если корутина была отменена, CancellationException уже обработана выше
                Log.e("DaData", "Ошибка запроса", e)
                _addressSuggestions.value = emptyList()
            }
        }
    }

    fun saveData(address: String) {
        // Сохраняем полный адрес в поле address
        cache.address = address
    }

    override fun onCleared() {
        super.onCleared()
        // Отменяем все корутины при уничтожении ViewModel
        searchJob?.cancel()
    }
}