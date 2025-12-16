package ru.otus.basicarchitecture.ui.interests

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.otus.basicarchitecture.WizardCache
import javax.inject.Inject

@HiltViewModel
class InterestsViewModel @Inject constructor(
    private val cache: WizardCache
) : ViewModel() {

    // Список доступных интересов
    val interests = listOf("Спорт", "Музыка", "Кино", "Путешествия", "Игры")

    /**
     * Сохранение выбранных интересов в кеш
     */
    fun savedInterests(selected: List<String>) {
        cache.interests = selected
    }
}

