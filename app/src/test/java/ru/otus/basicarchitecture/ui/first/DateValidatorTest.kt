package ru.otus.basicarchitecture.ui.first

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Unit-тесты для функции валидации даты рождения
 */
class DateValidatorTest {

    @Test
    fun `isAdult возвращает true для возраста 18+`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -20)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        val result = DateValidator.isAdult(birthDate)

        assertTrue(result)
    }

    @Test
    fun `isAdult возвращает false для возраста меньше 18`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -10)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        val result = DateValidator.isAdult(birthDate)

        assertFalse(result)
    }

    @Test
    fun `isAdult возвращает false для невалидной даты`() {
        val result = DateValidator.isAdult("32.13.2000")
        assertFalse(result)
    }

    @Test
    fun `isAdult возвращает true для неполной даты`() {
        val result = DateValidator.isAdult("01.01.20")
        assertTrue(result)
    }

    @Test
    fun `isAdult возвращает true для возраста ровно 18 лет`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        val result = DateValidator.isAdult(birthDate)

        assertTrue(result)
    }
}
