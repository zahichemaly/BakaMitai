package com.zc.bakamitai.ui.base

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VBinding : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: VBinding
    protected abstract fun getViewBinding(): VBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        setupView()
        manageSubscriptions()
        manageListeners()
    }

    protected fun hideActionBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
    }

    open fun setupView() {}
    open fun manageSubscriptions() {}
    open fun manageListeners() {}
}
