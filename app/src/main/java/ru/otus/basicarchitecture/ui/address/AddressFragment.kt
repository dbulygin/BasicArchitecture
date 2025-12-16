package ru.otus.basicarchitecture.ui.address

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.otus.basicarchitecture.R
import ru.otus.basicarchitecture.databinding.FragmentAddressBinding

@AndroidEntryPoint
class AddressFragment : Fragment() {
    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddressViewModel by viewModels()
    
    // Адаптер для отображения подсказок в AutoCompleteTextView
    private lateinit var suggestionsAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Адаптер для подсказок с кастомным фильтром
        // Фильтр не фильтрует результаты, так как фильтрация уже выполнена на сервере
        suggestionsAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        ) {
            override fun getFilter(): Filter {
                val adapter = this
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        // Возвращаем все элементы без фильтрации
                        // так как фильтрация уже выполнена на сервере API Дадата
                        val results = FilterResults()
                        // Создаем список всех элементов из адаптера
                        val items = ArrayList<String>()
                        for (i in 0 until adapter.count) {
                            adapter.getItem(i)?.let { items.add(it) }
                        }
                        results.values = items
                        results.count = items.size
                        return results
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        // Уведомляем об изменении данных
                        if (results != null && results.count > 0) {
                            notifyDataSetChanged()
                        } else {
                            notifyDataSetInvalidated()
                        }
                    }
                }
            }
        }
        binding.etAddress.setAdapter(suggestionsAdapter)
        // Устанавливаем threshold = 0, чтобы подсказки отображались сразу
        binding.etAddress.threshold = 0

        // Добавляем слушатель выбора элемента, после нажатия подсказка должна скрыться
        binding.etAddress.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            binding.etAddress.post {
                binding.etAddress.dismissDropDown()
            }
        }

        // Подписка на изменения подсказок из ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addressSuggestions.collect { suggestions ->
                // Обновляем адаптер
                suggestionsAdapter.clear()
                if (suggestions.isNotEmpty()) {
                    suggestionsAdapter.addAll(suggestions)
                    suggestionsAdapter.notifyDataSetChanged()

                    // Используем post для отложенного показа, чтобы избежать конфликтов
                    binding.etAddress.post {
                        val hasFocus = binding.etAddress.hasFocus()
                        val hasText = binding.etAddress.text.isNotEmpty()
                        val hasSuggestions = suggestionsAdapter.count > 0
                        
                        if (hasFocus && hasText && hasSuggestions) {
                            // Принудительно показываем dropdown
                            binding.etAddress.showDropDown()
                        } else if (!hasSuggestions) {
                            // Скрываем dropdown если подсказок нет
                            binding.etAddress.dismissDropDown()
                        }
                    }
                } else {
                    suggestionsAdapter.notifyDataSetChanged()
                    // Скрываем dropdown если подсказок нет
                    binding.etAddress.dismissDropDown()
                }
            }
        }

        // Обработчик изменения текста в поле ввода
        binding.etAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Запрашиваем подсказки при изменении текста
                val query = s?.toString() ?: ""
                viewModel.getAddressSuggestions(query)
            }

            override fun afterTextChanged(s: Editable?) {
                // После изменения текста проверяем, нужно ли показать dropdown
                if (suggestionsAdapter.count > 0 && binding.etAddress.hasFocus()) {
                    binding.etAddress.post {
                        if (binding.etAddress.hasFocus() && suggestionsAdapter.count > 0) {
                            binding.etAddress.showDropDown()
                        }
                    }
                }
            }
        })
        
        // Обработчик фокуса для показа подсказок при получении фокуса
        binding.etAddress.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && suggestionsAdapter.count > 0 && binding.etAddress.text.isNotEmpty()) {
                binding.etAddress.post {
                    if (binding.etAddress.hasFocus() && suggestionsAdapter.count > 0) {
                        binding.etAddress.showDropDown()
                    }
                }
            }
        }

        // Обработчик нажатия кнопки "Далее"
        binding.btnNext.setOnClickListener {
            val address = binding.etAddress.text.toString()

            viewModel.saveData(address)
            findNavController().navigate(R.id.action_addressFragment_to_interestsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

