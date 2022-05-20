package com.news.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Precision
import coil.transform.RoundedCornersTransformation
import com.news.R
import com.news.databinding.ItemNewsBinding
import com.news.model.domain.NewsModel

class NewsAdapter(
    val callback: NewsAdapterCallback
) : PagingDataAdapter<NewsModel, NewsAdapter.NewsViewHolder>(
    REPO_COMPARATOR
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class NewsViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemModel: NewsModel?) {
            itemModel?.let {
                val cornerRadius =
                    binding.root.resources.getDimensionPixelSize(R.dimen.news_image_corner_radius)
                        .toFloat()
                binding.tvSource.text = it.source
                binding.tvTitle.text = it.title
                binding.imgNews.load(it.picture) {
                    error(R.drawable.ic_news)
                    precision(Precision.INEXACT)// high quality
                    placeholder(R.drawable.img_loading_placeholder)
                    transformations(
                        RoundedCornersTransformation(
                            cornerRadius,
                            cornerRadius,
                            cornerRadius,
                            cornerRadius
                        )
                    )
                }
            }
        }

    }

    companion object {
        private val REPO_COMPARATOR = object : DiffUtil.ItemCallback<NewsModel>() {
            override fun areItemsTheSame(
                oldItem: NewsModel,
                newItem: NewsModel
            ): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(
                oldItem: NewsModel,
                newItem: NewsModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}

interface NewsAdapterCallback {
    fun onAddToFavoriteClicked(item: NewsModel)
    fun onShareBtnClicked(item: NewsModel)
    fun onSaveImageClicked(item: NewsModel)
}