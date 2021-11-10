package com.jingom.rssreader.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jingom.rssreader.R
import com.jingom.rssreader.model.Article

class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {
	private val articles: MutableList<Article> = mutableListOf()

	fun add(articles: List<Article>) {
		val originalSize = articles.size
		this.articles.addAll(articles)

		notifyItemRangeInserted(originalSize, articles.size)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val layout = LayoutInflater.from(parent.context)
			.inflate(R.layout.article, parent, false) as LinearLayout

		val feed = layout.findViewById<TextView>(R.id.feed)
		val title = layout.findViewById<TextView>(R.id.title)
		val summary = layout.findViewById<TextView>(R.id.summary)

		return ViewHolder(layout, feed, title, summary)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val article = articles[position]

		holder.bind(article)
	}

	override fun getItemCount() = articles.size

	class ViewHolder(
		val layout: LinearLayout,
		val feed: TextView,
		val title: TextView,
		val summary: TextView
	) : RecyclerView.ViewHolder(layout) {

		fun bind(article: Article) {
			feed.text = article.feed
			title.text = article.title
			summary.text = article.summary
		}
	}
}