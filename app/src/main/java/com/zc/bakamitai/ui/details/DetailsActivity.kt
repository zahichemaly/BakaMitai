package com.zc.bakamitai.ui.details

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.zc.bakamitai.R
import com.zc.bakamitai.data.models.Resource
import com.zc.bakamitai.data.models.dtos.ShowDetailsDto
import com.zc.bakamitai.databinding.ActivityDetailsBinding
import com.zc.bakamitai.extensions.hide
import com.zc.bakamitai.extensions.invisible
import com.zc.bakamitai.extensions.show
import com.zc.bakamitai.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsActivity : BaseActivity<ActivityDetailsBinding>() {
    private val viewModel: DetailsViewModel by viewModel()
    override fun getViewBinding() = ActivityDetailsBinding.inflate(layoutInflater)
    private lateinit var page: String

    override fun onCreate(savedInstanceState: Bundle?) {
        hideActionBar()
        super.onCreate(savedInstanceState)
        page = intent.getStringExtra(PAGE)!!
        viewModel.getShowDetails(page)
    }

    override fun manageSubscriptions() {
        viewModel.show.observe(this) {
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    binding.contentLayout.show()
                    bindData(it.data!!)
                }
                is Resource.Error -> {
                    binding.progressBar.invisible()
                }
                is Resource.Loading -> {
                    binding.progressBar.show()
                }
            }
        }
        viewModel.isBookmarked.observe(this) {
            val bg = if (it) ContextCompat.getDrawable(this, R.drawable.ic_bookmark_filled)
            else ContextCompat.getDrawable(this, R.drawable.ic_bookmark_empty)
            binding.ivBookmark.setImageDrawable(bg)
        }
    }

    override fun manageListeners() {
        binding.ivBookmark.setOnClickListener {
            viewModel.saveOrRemoveBookmark()
        }
    }

    private fun bindData(showDetailsDto: ShowDetailsDto) {
        binding.tvName.text = showDetailsDto.title
        binding.expandTextView.text = showDetailsDto.synopsis
        Glide.with(this)
            .load(showDetailsDto.image)
            .into(binding.ivEntry)
        viewModel.setIsBookmarked(showDetailsDto.sid!!)
    }

    companion object {
        const val PAGE = "page"
    }
}
