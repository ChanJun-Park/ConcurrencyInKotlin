package com.jingom.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jingom.rssreader.adapter.ArticleAdapter
import com.jingom.rssreader.databinding.ActivityMainBinding
import com.jingom.rssreader.model.Article
import com.jingom.rssreader.model.Feed
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {

	private val netDispatcher = newFixedThreadPoolContext(name = "IO", nThreads = 2)
	private val factory = DocumentBuilderFactory.newInstance()
	private lateinit var binding: ActivityMainBinding

	private lateinit var viewAdapter: ArticleAdapter
	private lateinit var viewManager: RecyclerView.LayoutManager

	private val feeds = listOf(
		Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
		Feed("cnn", "https://rss.cnn.com/rss/cnn_topstories.rss"),
		Feed("fox", "https://feeds.foxnews.com/foxnews/latest?format=xml"),
		Feed("yonhapnews", "https://www.yonhapnewstv.co.kr/category/news/headline/feed/"),
		Feed("inv", "htt://www")
	)

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

		asyncLoadNews()
	}

	private fun asyncLoadNews() = GlobalScope.launch {
		val requests = mutableListOf<Deferred<List<Article>>>()

		feeds.mapTo(requests) {
			asyncFetchArticles(it, netDispatcher)
		}

		requests.forEach {
//			it.await()
			it.join()
		}

		val articles = requests
			.filter { !it.isCancelled }
			.flatMap { it.getCompleted() }

		val failed = requests
			.filter { it.isCancelled }
			.size

		val obtained = requests.size - failed

		GlobalScope.launch(Dispatchers.Main) {
			binding.progressBar.visibility = View.GONE
			viewAdapter.add(articles)
		}
	}

	private fun asyncFetchArticles(feed: Feed, dispatcher: CoroutineDispatcher) = GlobalScope.async(dispatcher) {
		delay(1000)

		val builder = factory.newDocumentBuilder()
		val xml = builder.parse(feed.url)
		val news = xml.getElementsByTagName("channel").item(0)

		(0 until news.childNodes.length)
			.map { news.childNodes.item(it) }
			.filter { Node.ELEMENT_NODE == it.nodeType }
			.map { it as Element }
			.filter { "item" == it.tagName }
			.map {
				val title = it.getElementsByTagName("title").item(0).textContent
				var summary = it.getElementsByTagName("description").item(0).textContent

				if (!summary.startsWith("<div") && summary.contains("<div")) {
					summary = summary.substring(0, summary.indexOf("<div"))
				}

				Article(feed.name, title, summary)
			}
	}
}