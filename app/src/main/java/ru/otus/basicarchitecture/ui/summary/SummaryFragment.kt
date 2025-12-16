package ru.otus.basicarchitecture.ui.summary

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.basicarchitecture.databinding.FragmentSummaryBinding

@AndroidEntryPoint
class SummaryFragment : Fragment() {
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SummaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cache = viewModel.getData()
        binding.tvResult.text = """
            Имя: ${cache.firstName}
            Фамилия: ${cache.lastName}
            Дата рождения: ${cache.birthDate}
            Адрес: ${cache.address}
            Интересы: ${cache.interests.joinToString(", ")}
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

