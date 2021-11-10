package com.jingom.rssreader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jingom.rssreader.adapter.ArticleAdapter
import com.jingom.rssreader.databinding.ActivitySearchBinding
import com.jingom.rssreader.search.Searcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySearchBinding
	private lateinit var viewAdapter: ArticleAdapter
	private lateinit var viewManager: RecyclerView.LayoutManager
	private val searcher = Searcher()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySearchBinding.inflate(layoutInflater)
		setContentView(binding.root)

		viewManager = LinearLayoutManager(this)
		viewAdapter = ArticleAdapter()
		binding.articles.run {
			layoutManager = viewManager
			adapter = viewAdapter
		}

		binding.searchButton.setOnClickListener {
			viewAdapter.clear()
			GlobalScope.launch {
				search()
			}
		}
	}

	private suspend fun search() {
		val query = binding.searchText.text.toString()

		val channel = searcher.search(query)

		while(!channel.isClosedForReceive) {
			val article = channel.receive()

			GlobalScope.launch(Dispatchers.Main) {
				viewAdapter.add(article)
			}
		}
	}
}