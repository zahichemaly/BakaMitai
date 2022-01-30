package com.zc.bakamitai.ui.details

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.zc.bakamitai.R
import com.zc.bakamitai.data.Constants
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
    private var show: ShowDetailsDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        hideActionBar()
        super.onCreate(savedInstanceState)
        page = intent.getStringExtra(Constants.Intent.PAGE)!!
        viewModel.getShowDetails(page)
    }

    override fun manageSubscriptions() {
        viewModel.show.observe(this) {
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    binding.contentLayout.show()
                    show = it.data!!
                    bindData(show!!)
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
            binding.ibBookmark.setImageDrawable(bg)
        }
    }

    override fun manageListeners() {
        binding.ibBookmark.setOnClickListener {
            viewModel.saveOrRemoveBookmark()
        }
        binding.ibShare.setOnClickListener {
            share()
        }
    }

    private fun share() {
        show?.let {
            val text = getString(R.string.share_text, it.getPageUrl())
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(intent, "Share To:"))
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
}
