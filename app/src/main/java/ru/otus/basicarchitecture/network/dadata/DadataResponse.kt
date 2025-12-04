package ru.otus.basicarchitecture.network.dadata

// Модель ответа от API Дадата
data class DadataResponse(
    val suggestions: List<Suggestion>
)

// Модель подсказки адреса
data class Suggestion(
    val value: String,
    val unrestricted_value: String
)

