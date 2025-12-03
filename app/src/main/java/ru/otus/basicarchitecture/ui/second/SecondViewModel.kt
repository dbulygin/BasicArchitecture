package ru.otus.basicarchitecture.ui.second

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.otus.basicarchitecture.WizardCache
import javax.inject.Inject

@HiltViewModel
class SecondViewModel @Inject constructor(
    private val cache: WizardCache
) : ViewModel() {

    fun saveData(country: String, city: String, address: String) {
        cache.country = country
        cache.city = city
        cache.address = address
    }
}