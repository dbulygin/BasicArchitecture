// FirstFragment.kt
package ru.otus.basicarchitecture.ui.first

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
import ru.otus.basicarchitecture.databinding.FragmentFirstBinding

@AndroidEntryPoint
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FirstViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etFirstName.addTextChangedListener(textWatcher { viewModel.onFirstNameChange(it) })
        binding.etLastName.addTextChangedListener(textWatcher { viewModel.onLastNameChange(it) })
        binding.etBirthDate.addTextChangedListener(dateTextWatcher())

        binding.btnNext.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.isValid) {
                viewModel.saveAndProceed()
                findNavController().navigate(R.id.action_firstFragment_to_secondFragment)
            } else {
                // Показываем Toast с ошибкой если она есть
                state.error?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.btnNext.isEnabled = state.isValid
            }
        }
    }

    private fun textWatcher(onChange: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onChange(s.toString())
        }
    }

    private fun dateTextWatcher() = object : TextWatcher {
        private var isUpdating = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isUpdating) return

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

            // Проверяем полную дату и показываем Toast если есть ошибка
            if (text.length == 10) {
                val state = viewModel.uiState.value
                if (!state.isValid && state.error != null) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
