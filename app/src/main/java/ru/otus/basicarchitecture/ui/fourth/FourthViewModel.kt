package ru.otus.basicarchitecture.ui.fourth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.otus.basicarchitecture.WizardCache
import javax.inject.Inject

@HiltViewModel
class FourthViewModel @Inject constructor(
    val cache: WizardCache
) : ViewModel() {
    fun getData() = cache
}