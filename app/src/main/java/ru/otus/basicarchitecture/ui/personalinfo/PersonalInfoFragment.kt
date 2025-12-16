package ru.otus.basicarchitecture.ui.personalinfo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.otus.basicarchitecture.R
import ru.otus.basicarchitecture.databinding.FragmentPersonalInfoBinding

@AndroidEntryPoint
class PersonalInfoFragment : Fragment() {
    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonalInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем слушатели изменения текста для полей ввода
        binding.etFirstName.addTextChangedListener(textWatcher { viewModel.onFirstNameChange(it) })
        binding.etLastName.addTextChangedListener(textWatcher { viewModel.onLastNameChange(it) })
        binding.etBirthDate.addTextChangedListener(dateTextWatcher())

        // Обработчик нажатия кнопки "Далее"
        binding.btnNext.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.isValid) {
                viewModel.saveAndProceed()
                findNavController().navigate(R.id.action_personalInfoFragment_to_addressFragment)
            } else {
                // Показываем Toast с ошибкой, если она есть
                state.error?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Подписка на изменения состояния UI
        viewLifecycleOwner.lifecycleScope.launch {
            var previousError: String? = null
            viewModel.uiState.collectLatest { state ->
                binding.btnNext.isEnabled = state.isValid
                
                // Показываем Toast только если ошибка изменилась и дата полностью введена
                if (state.birthDate.length == 10 && 
                    state.error != null && 
                    state.error != previousError) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                }
                previousError = state.error
            }
        }
    }

    /**
     * Создает TextWatcher для обработки изменений текста
     */
    private fun textWatcher(onChange: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onChange(s.toString())
        }
    }

    /**
     * Создает TextWatcher для поля даты рождения
     * Автоматически форматирует ввод в формат DD.MM.YYYY
     */
    private fun dateTextWatcher() = object : TextWatcher {
        private var isUpdating = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isUpdating) return

            // Удаляем точки и форматируем ввод
            val input = s.toString().replace(".", "")
            if (input.length > 8) return

            val formatted = StringBuilder()
            for (i in input.indices) {
                formatted.append(input[i])
                when (i) {
                    1, 3 -> formatted.append(".")
                }
            }

            isUpdating = true
            binding.etBirthDate.setText(formatted.toString())
            binding.etBirthDate.setSelection(formatted.length)
            isUpdating = false
        }

        override fun afterTextChanged(s: Editable?) {
            val text = s.toString()
            viewModel.onBirthDateChange(text)
            // Toast показывается через подписку на uiState в onViewCreated
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

