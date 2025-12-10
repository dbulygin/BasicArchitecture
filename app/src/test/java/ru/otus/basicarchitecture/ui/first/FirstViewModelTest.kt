package ru.otus.basicarchitecture.ui.first

import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.otus.basicarchitecture.WizardCache
import java.util.Calendar

/**
 * Unit-тесты для FirstViewModel
 * Тестируем валидацию ввода данных на первом экране
 */
class FirstViewModelTest {

    private lateinit var cache: WizardCache
    private lateinit var viewModel: FirstViewModel

    @Before
    fun setup() {
        // Создаем мок WizardCache перед каждым тестом
        cache = mockk(relaxed = true)
        viewModel = FirstViewModel(cache)
    }

    /**
     * Тест: проверка функции isAdult() для возраста 18+
     * Как: передаем дату рождения, которая дает возраст >= 18 лет
     * Ожидаем: функция возвращает true
     */
    @Test
    fun `isAdult возвращает true для возраста 18+`() {
        // Вычисляем дату 20 лет назад
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -20)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        val result = viewModel.isAdult(birthDate)

        assertTrue("Возраст 20 лет должен быть >= 18", result)
    }

    /**
     * Тест: проверка функции isAdult() для возраста < 18
     * Как: передаем дату рождения, которая дает возраст < 18 лет
     * Ожидаем: функция возвращает false
     */
    @Test
    fun `isAdult возвращает false для возраста меньше 18`() {
        // Вычисляем дату 10 лет назад
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -10)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        val result = viewModel.isAdult(birthDate)

        assertFalse("Возраст 10 лет должен быть < 18", result)
    }

    /**
     * Тест: проверка функции isAdult() для невалидной даты
     * Как: передаем невалидную дату (неправильный формат)
     * Ожидаем: функция возвращает false
     */
    @Test
    fun `isAdult возвращает false для невалидной даты`() {
        val invalidDate = "32.13.2000"

        val result = viewModel.isAdult(invalidDate)

        assertFalse("Невалидная дата должна возвращать false", result)
    }

    /**
     * Тест: проверка функции isAdult() для неполной даты
     * Как: передаем дату длиной < 10 символов
     * Ожидаем: функция возвращает true (не валидируется)
     */
    @Test
    fun `isAdult возвращает true для неполной даты`() {
        val incompleteDate = "01.01.20"

        val result = viewModel.isAdult(incompleteDate)

        assertTrue("Неполная дата должна возвращать true", result)
    }

    /**
     * Тест: проверка валидации когда все поля пустые
     * Как: создаем ViewModel и проверяем начальное состояние (validate вызывается в init)
     * Ожидаем: ошибка "Введите имя", isValid = false
     */
    @Test
    fun `validate показывает ошибку когда все поля пустые`() = runTest {
        // ViewModel вызывает validate() в init, поэтому начальное состояние уже валидировано
        val state = viewModel.uiState.first()

        assertEquals("Введите имя", state.error)
        assertFalse(state.isValid)
    }

    /**
     * Тест: проверка валидации когда заполнено только имя
     * Как: вводим имя, остальные поля оставляем пустыми
     * Ожидаем: ошибка "Введите фамилию", isValid = false
     */
    @Test
    fun `validate показывает ошибку когда заполнено только имя`() = runTest {
        viewModel.onFirstNameChange("Иван")

        val state = viewModel.uiState.first()

        assertEquals("Введите фамилию", state.error)
        assertFalse(state.isValid)
    }

    /**
     * Тест: проверка валидации когда заполнены имя и фамилия
     * Как: вводим имя и фамилию, дату оставляем пустой
     * Ожидаем: ошибка "Введите дату рождения", isValid = false
     */
    @Test
    fun `validate показывает ошибку когда заполнены имя и фамилия`() = runTest {
        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Иванов")

        val state = viewModel.uiState.first()

        assertEquals("Введите дату рождения", state.error)
        assertFalse(state.isValid)
    }

    /**
     * Тест: проверка валидации когда дата неполная
     * Как: вводим имя, фамилию и неполную дату (< 10 символов)
     * Ожидаем: ошибки нет (null), isValid = false (так как дата неполная, но не пустая)
     */
    @Test
    fun `validate не показывает ошибку когда дата неполная`() = runTest {
        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Иванов")
        viewModel.onBirthDateChange("01.01.20")

        val state = viewModel.uiState.first()

        assertNull("Ошибки не должно быть для неполной даты", state.error)
        // isValid = false для неполной даты (дата не пустая, но не полная)
        assertFalse("isValid должен быть false для неполной даты", state.isValid)
    }

    /**
     * Тест: проверка валидации когда дата валидная но возраст < 18
     * Как: вводим все поля, дату рождения для возраста < 18
     * Ожидаем: ошибка "Возраст должен быть 18+", isValid = false
     */
    @Test
    fun `validate показывает ошибку когда возраст меньше 18`() = runTest {
        // Вычисляем дату 10 лет назад
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -10)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Иванов")
        viewModel.onBirthDateChange(birthDate)

        val state = viewModel.uiState.first()

        assertEquals("Возраст должен быть 18+", state.error)
        assertFalse(state.isValid)
    }

    /**
     * Тест: проверка валидации когда все поля валидны
     * Как: вводим все поля с валидными данными (возраст >= 18)
     * Ожидаем: ошибки нет (null), isValid = true
     */
    @Test
    fun `validate проходит успешно когда все поля валидны`() = runTest {
        // Вычисляем дату 20 лет назад
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -20)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Иванов")
        viewModel.onBirthDateChange(birthDate)

        val state = viewModel.uiState.first()

        assertNull("Ошибки не должно быть", state.error)
        assertTrue("isValid должен быть true", state.isValid)
    }

    /**
     * Тест: проверка обновления состояния при изменении имени
     * Как: вызываем onFirstNameChange() с новым значением
     * Ожидаем: состояние обновляется, валидация вызывается
     */
    @Test
    fun `onFirstNameChange обновляет состояние и вызывает валидацию`() = runTest {
        viewModel.onFirstNameChange("Петр")

        val state = viewModel.uiState.first()

        assertEquals("Петр", state.firstName)
        // Валидация должна была вызваться, но так как остальные поля пустые, будет ошибка
        assertEquals("Введите фамилию", state.error)
    }

    /**
     * Тест: проверка обновления состояния при изменении фамилии
     * Как: вызываем onLastNameChange() с новым значением
     * Ожидаем: состояние обновляется, валидация вызывается
     */
    @Test
    fun `onLastNameChange обновляет состояние и вызывает валидацию`() = runTest {
        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Петров")

        val state = viewModel.uiState.first()

        assertEquals("Петров", state.lastName)
        // Валидация должна была вызваться, но так как дата пустая, будет ошибка
        assertEquals("Введите дату рождения", state.error)
    }

    /**
     * Тест: проверка обновления состояния при изменении даты
     * Как: вызываем onBirthDateChange() с новым значением
     * Ожидаем: состояние обновляется, валидация вызывается
     */
    @Test
    fun `onBirthDateChange обновляет состояние и вызывает валидацию`() = runTest {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -20)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Иванов")
        viewModel.onBirthDateChange(birthDate)

        val state = viewModel.uiState.first()

        assertEquals(birthDate, state.birthDate)
        // Валидация должна была вызваться, все поля валидны
        assertNull(state.error)
        assertTrue(state.isValid)
    }
}

