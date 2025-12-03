package ru.otus.basicarchitecture.ui.third

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.otus.basicarchitecture.WizardCache
import javax.inject.Inject

@HiltViewModel
class ThirdViewModel @Inject constructor(
    private val cache: WizardCache
) : ViewModel() {

    val interests = listOf("Спорт", "Музыка", "Кино", "Путешествия", "Игры")

    fun savedInterests(selected: List<String>) {
        cache.interests = selected
    }
}