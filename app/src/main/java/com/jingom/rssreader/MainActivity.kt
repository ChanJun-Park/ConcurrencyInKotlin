package com.jingom.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jingom.rssreader.adapter.ArticleAdapter
import com.jingom.rssreader.adapter.ArticleLoader
import com.jingom.rssreader.databinding.ActivityMainBinding
import com.jingom.rssreader.model.Article
import com.jingom.rssreader.model.Feed
import com.jingom.rssreader.producer.ArticleProducer
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity(), ArticleLoader {

	private lateinit var binding: ActivityMainBinding

	private lateinit var viewAdapter: ArticleAdapter
	private lateinit var viewManager: RecyclerView.LayoutManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		viewManager = LinearLayoutManager(this)
		viewAdapter = ArticleAdapter()
		binding.articles.run {
			layoutManager = viewManager
			adapter = viewAdapter
		}

		GlobalScope.launch {
			loadMore()
		}
	}

	override suspend fun loadMore() {
		val producer = ArticleProducer.producer

		if (!producer.isClosedForReceive) {
			val articles = producer.receive()

			GlobalScope.launch(Dispatchers.Main) {
				binding.progressBar.visibility = View.GONE
				viewAdapter.add(articles)
			}
		}
	}

//	private fun asyncLoadNews() = GlobalScope.launch {
//		val requests = mutableListOf<Deferred<List<Article>>>()
//
//		feeds.mapTo(requests) {
//			asyncFetchArticles(it, netDispatcher)
//		}
//
//		requests.forEach {
////			it.await()
//			it.join()
//		}
//
//		val articles = requests
//			.filter { !it.isCancelled }
//			.flatMap { it.getCompleted() }
//
//		val failed = requests
//			.filter { it.isCancelled }
//			.size
//
//		val obtained = requests.size - failed
//
//		GlobalScope.launch(Dispatchers.Main) {
//			binding.progressBar.visibility = View.GONE
//			viewAdapter.add(articles)
//		}
//	}
}