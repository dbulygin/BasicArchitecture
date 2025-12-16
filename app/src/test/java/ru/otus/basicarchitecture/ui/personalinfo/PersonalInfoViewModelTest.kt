package ru.otus.basicarchitecture.ui.personalinfo

import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.otus.basicarchitecture.WizardCache
import java.util.Calendar

class PersonalInfoViewModelTest {

    private lateinit var cache: WizardCache
    private lateinit var viewModel: PersonalInfoViewModel

    @Before
    fun setup() {
        cache = mockk(relaxed = true)
        viewModel = PersonalInfoViewModel(cache)
    }

    @Test
    fun `isAdult возвращает true для возраста 18+`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -20)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val birthDate = String.format("%02d.%02d.%04d", day, month, year)

        val result = viewModel.isAdult(birthDate)

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

        val result = viewModel.isAdult(birthDate)

        assertFalse(result)
    }

    @Test
    fun `isAdult возвращает false для невалидной даты`() {
        val result = viewModel.isAdult("32.13.2000")
        assertFalse(result)
    }

    @Test
    fun `isAdult возвращает true для неполной даты`() {
        val result = viewModel.isAdult("01.01.20")
        assertTrue(result)
    }

    @Test
    fun `validate показывает ошибку когда все поля пустые`() = runTest {
        val state = viewModel.uiState.first()

        assertEquals("Введите имя", state.error)
        assertFalse(state.isValid)
    }

    @Test
    fun `validate показывает ошибку когда заполнено только имя`() = runTest {
        viewModel.onFirstNameChange("Иван")

        val state = viewModel.uiState.first()

        assertEquals("Введите фамилию", state.error)
        assertFalse(state.isValid)
    }

    @Test
    fun `validate показывает ошибку когда заполнены имя и фамилия`() = runTest {
        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Иванов")

        val state = viewModel.uiState.first()

        assertEquals("Введите дату рождения", state.error)
        assertFalse(state.isValid)
    }

    @Test
    fun `validate не показывает ошибку когда дата неполная`() = runTest {
        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Иванов")
        viewModel.onBirthDateChange("01.01.20")

        val state = viewModel.uiState.first()

        assertNull(state.error)
        assertFalse(state.isValid)
    }

    @Test
    fun `validate показывает ошибку когда возраст меньше 18`() = runTest {
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

    @Test
    fun `validate проходит успешно когда все поля валидны`() = runTest {
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

        assertNull(state.error)
        assertTrue(state.isValid)
    }

    @Test
    fun `onFirstNameChange обновляет состояние и вызывает валидацию`() = runTest {
        viewModel.onFirstNameChange("Петр")

        val state = viewModel.uiState.first()

        assertEquals("Петр", state.firstName)
        assertEquals("Введите фамилию", state.error)
    }

    @Test
    fun `onLastNameChange обновляет состояние и вызывает валидацию`() = runTest {
        viewModel.onFirstNameChange("Иван")
        viewModel.onLastNameChange("Петров")

        val state = viewModel.uiState.first()

        assertEquals("Петров", state.lastName)
        assertEquals("Введите дату рождения", state.error)
    }

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
        assertNull(state.error)
        assertTrue(state.isValid)
    }
}

