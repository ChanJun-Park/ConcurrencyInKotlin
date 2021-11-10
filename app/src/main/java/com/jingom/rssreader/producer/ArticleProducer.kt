package com.jingom.rssreader.producer

import com.jingom.rssreader.model.Article
import com.jingom.rssreader.model.Feed
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

object ArticleProducer {

	private val netDispatcher = newFixedThreadPoolContext(name = "IO", nThreads = 2)
	private val factory = DocumentBuilderFactory.newInstance()

	private val feeds = listOf(
		Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
		Feed("cnn", "https://rss.cnn.com/rss/cnn_topstories.rss"),
		Feed("fox", "https://feeds.foxnews.com/foxnews/latest?format=xml"),
		Feed("yonhapnews", "https://www.yonhapnewstv.co.kr/category/news/headline/feed/"),
	)

	val producer = GlobalScope.produce(netDispatcher) {
		feeds.forEach {
			send(fetchArticles(it))
		}
	}

	private fun fetchArticles(feed: Feed) : List<Article> {

		val builder = factory.newDocumentBuilder()
		val xml = builder.parse(feed.url)
		val news = xml.getElementsByTagName("channel").item(0)

		return (0 until news.childNodes.length)
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