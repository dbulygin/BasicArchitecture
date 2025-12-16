package ru.otus.basicarchitecture.ui.personalinfo

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Валидатор для проверки даты рождения и возраста
 * Вынесен в отдельный класс для удобства тестирования
 */
object DateValidator {
    /**
     * Проверяет, является ли пользователь совершеннолетним
     */
    fun isAdult(birthDate: String): Boolean {
        return try {
            if (birthDate.length < 10) return true

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(birthDate) ?: return false

            val birthCalendar = Calendar.getInstance().apply { time = date }
            val currentCalendar = Calendar.getInstance()

            var age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            age >= 18
        } catch (e: Exception) {
            false
        }
    }
}

