package ru.otus.basicarchitecture.network.dadata

// Модель запроса к API Дадата
data class DadataRequest(
    val query: String,
    val count: Int = 10
)

