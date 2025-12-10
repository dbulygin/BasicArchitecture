package ru.otus.basicarchitecture.ui.second

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.otus.basicarchitecture.WizardCache
import ru.otus.basicarchitecture.network.dadata.DadataApi
import ru.otus.basicarchitecture.network.dadata.DadataRequest
import ru.otus.basicarchitecture.network.dadata.DadataResponse
import ru.otus.basicarchitecture.network.dadata.Suggestion

/**
 * Unit-тесты для SecondViewModel
 * Тестируем сетевые запросы и обработку ответов от API Дадата
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SecondViewModelTest {

    private lateinit var cache: WizardCache
    private lateinit var dadataApi: DadataApi
    private lateinit var viewModel: SecondViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Настраиваем тестовый диспетчер для Main dispatcher
        Dispatchers.setMain(testDispatcher)
        
        // Создаем моки
        cache = mockk(relaxed = true)
        dadataApi = mockk(relaxed = true)
        
        // Создаем ViewModel
        viewModel = SecondViewModel(cache, dadataApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAddressSuggestions успешно получает подсказки`() = runTest(testDispatcher) {
        // Настраиваем мок
        val mockResponse = DadataResponse(
            listOf(
                Suggestion("г Москва, ул Ленина, д 1", "101000, г Москва, ул Ленина, д 1"),
                Suggestion("г Москва, ул Ленина, д 2", "101000, г Москва, ул Ленина, д 2")
            )
        )
        coEvery { dadataApi.getAddressSuggestions(any()) } returns mockResponse

        // Вызываем метод
        viewModel.getAddressSuggestions("москва")
        
        // Продвигаем время для debounce (500ms) и выполнения запроса
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        // Проверяем результат
        val suggestions = viewModel.addressSuggestions.first()
        assertEquals(2, suggestions.size)
        assertEquals("г Москва, ул Ленина, д 1", suggestions[0])
        assertEquals("г Москва, ул Ленина, д 2", suggestions[1])
        
        // Проверяем вызов API
        coVerify { dadataApi.getAddressSuggestions(DadataRequest(query = "москва", count = 10)) }
    }

    @Test
    fun `getAddressSuggestions очищает подсказки при коротком запросе`() = runTest(testDispatcher) {
        // Вызываем метод с коротким запросом
        viewModel.getAddressSuggestions("мо")
        
        // Продвигаем время
        testDispatcher.scheduler.runCurrent()

        // Проверяем, что подсказки пустые
        val suggestions = viewModel.addressSuggestions.first()
        assertTrue(suggestions.isEmpty())
        
        // Проверяем, что API не был вызван
        coVerify(exactly = 0) { dadataApi.getAddressSuggestions(any()) }
    }

    @Test
    fun `getAddressSuggestions обрабатывает ошибку сети`() = runTest(testDispatcher) {
        // Настраиваем мок для выброса исключения
        coEvery { dadataApi.getAddressSuggestions(any()) } throws Exception("Network error")

        // Вызываем метод
        viewModel.getAddressSuggestions("москва")
        
        // Продвигаем время
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        // Проверяем, что подсказки пустые
        val suggestions = viewModel.addressSuggestions.first()
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `getAddressSuggestions обрабатывает пустой ответ`() = runTest(testDispatcher) {
        // Настраиваем мок для пустого ответа
        coEvery { dadataApi.getAddressSuggestions(any()) } returns DadataResponse(emptyList())

        // Вызываем метод
        viewModel.getAddressSuggestions("несуществующий адрес")
        
        // Продвигаем время
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        // Проверяем, что подсказки пустые
        val suggestions = viewModel.addressSuggestions.first()
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `getAddressSuggestions обрезает пробелы в запросе`() = runTest(testDispatcher) {
        // Настраиваем мок
        coEvery { dadataApi.getAddressSuggestions(any()) } returns DadataResponse(emptyList())

        // Вызываем метод с пробелами
        viewModel.getAddressSuggestions("  москва  ")
        
        // Продвигаем время
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        // Проверяем, что API был вызван с обрезанным запросом
        coVerify { dadataApi.getAddressSuggestions(DadataRequest(query = "москва", count = 10)) }
    }
}
