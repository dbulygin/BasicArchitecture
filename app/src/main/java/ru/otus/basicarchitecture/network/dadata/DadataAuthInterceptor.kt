package ru.otus.basicarchitecture.network.dadata

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Интерцептор для добавления заголовков аутентификации к запросам API Дадата
 * Добавляет Authorization и X-Secret заголовки
 */
class DadataAuthInterceptor(
    private val apiKey: String,
    private val secretKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Добавляем заголовки аутентификации
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Token $apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("X-Secret", secretKey)
            .build()

        return chain.proceed(authenticatedRequest)
    }
}


