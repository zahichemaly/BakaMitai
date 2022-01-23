package com.zc.bakamitai.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.zc.bakamitai.R
import com.zc.bakamitai.databinding.LoadingViewBinding
import com.zc.bakamitai.extensions.hide
import com.zc.bakamitai.extensions.invisible
import com.zc.bakamitai.extensions.show
import timber.log.Timber

class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val _binding: LoadingViewBinding = LoadingViewBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    val binding: LoadingViewBinding
        get() = _binding

    fun setSuccess() {
        Timber.d("Success!")
        hide()
    }

    fun setLoading() {
        Timber.d("Loading...")
        show()
        binding.ivError.invisible()
        binding.progressBar.show()
        binding.tvError.hide()
    }

    fun setError() {
        show()
        binding.ivError.show()
        binding.progressBar.hide()
        binding.tvError.show()
        binding.tvError.text = getRandomErrorMessage()
    }

    fun setMessage(text: String) {
        Timber.d("Custom message: $text")
        show()
        binding.ivError.show()
        binding.progressBar.hide()
        binding.tvError.show()
        binding.tvError.text = text
    }

    private fun getRandomErrorMessage(): String {
        val errors = context.resources.getStringArray(R.array.error_messages)
        val randomIndex = (errors.indices).random()
        return errors[randomIndex].also {
            Timber.d("Error: $it")
        }
    }
}
