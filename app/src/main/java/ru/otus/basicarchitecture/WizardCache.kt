package ru.otus.basicarchitecture

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WizardCache @Inject constructor() {
    var firstName: String = ""
    var lastName: String = ""
    var birthDate: String = ""
    var address: String = ""
    var interests: List<String> = emptyList()
}
