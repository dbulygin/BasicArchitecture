package ru.otus.basicarchitecture

import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Кеш для хранения данных мастера регистрации
 * Использует ActivityRetainedScoped - данные сохраняются при повороте экрана,
 * но очищаются при уничтожении Activity
 */
@ActivityRetainedScoped
class WizardCache @Inject constructor() {
    var firstName: String = ""
    var lastName: String = ""
    var birthDate: String = ""
    var address: String = ""
    var interests: List<String> = emptyList()
}
