package ru.otus.basicarchitecture.ui.address

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

@OptIn(ExperimentalCoroutinesApi::class)
class AddressViewModelTest {

    private lateinit var cache: WizardCache
    private lateinit var dadataApi: DadataApi
    private lateinit var viewModel: AddressViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        cache = mockk(relaxed = true)
        dadataApi = mockk(relaxed = true)
        
        viewModel = AddressViewModel(cache, dadataApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAddressSuggestions успешно получает подсказки`() = runTest(testDispatcher) {
        val mockResponse = DadataResponse(
            listOf(
                Suggestion("г Москва, ул Ленина, д 1", "101000, г Москва, ул Ленина, д 1"),
                Suggestion("г Москва, ул Ленина, д 2", "101000, г Москва, ул Ленина, д 2")
            )
        )
        coEvery { dadataApi.getAddressSuggestions(any()) } returns mockResponse

        viewModel.getAddressSuggestions("москва")
        
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        val suggestions = viewModel.addressSuggestions.first()
        assertEquals(2, suggestions.size)
        assertEquals("г Москва, ул Ленина, д 1", suggestions[0])
        assertEquals("г Москва, ул Ленина, д 2", suggestions[1])
        
        coVerify { dadataApi.getAddressSuggestions(DadataRequest(query = "москва", count = 10)) }
    }

    @Test
    fun `getAddressSuggestions очищает подсказки при коротком запросе`() = runTest(testDispatcher) {
        viewModel.getAddressSuggestions("мо")
        
        testDispatcher.scheduler.runCurrent()

        val suggestions = viewModel.addressSuggestions.first()
        assertTrue(suggestions.isEmpty())
        
        coVerify(exactly = 0) { dadataApi.getAddressSuggestions(any()) }
    }

    @Test
    fun `getAddressSuggestions обрабатывает ошибку сети`() = runTest(testDispatcher) {
        coEvery { dadataApi.getAddressSuggestions(any()) } throws Exception("Network error")

        viewModel.getAddressSuggestions("москва")
        
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        val suggestions = viewModel.addressSuggestions.first()
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `getAddressSuggestions обрабатывает пустой ответ`() = runTest(testDispatcher) {
        coEvery { dadataApi.getAddressSuggestions(any()) } returns DadataResponse(emptyList())

        viewModel.getAddressSuggestions("несуществующий адрес")
        
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        val suggestions = viewModel.addressSuggestions.first()
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `getAddressSuggestions обрезает пробелы в запросе`() = runTest(testDispatcher) {
        coEvery { dadataApi.getAddressSuggestions(any()) } returns DadataResponse(emptyList())

        viewModel.getAddressSuggestions("  москва  ")
        
        testDispatcher.scheduler.advanceTimeBy(600)
        testDispatcher.scheduler.runCurrent()

        coVerify { dadataApi.getAddressSuggestions(DadataRequest(query = "москва", count = 10)) }
    }
}

