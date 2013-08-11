package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import scala.xml.NodeSeq
import code.lib.StopList

/*
 * Receives student responses whenever they press space
 * For now creates the wordle, later will pre-process for clustering
 */
class ResponseCloudServer extends LiftActor with ListenerManager {
	var studentResponses: Map[Long, WordCounter] = Map()
	val selectAmount = 15

	override def lowPriority = {
		case LiveUpdate(id, text) =>
			studentResponses.get(id) match {
				case Some(wordCounter) =>
					wordCounter.update(text)
				case None => studentResponses += id -> new WordCounter(text)
			}
			updateListeners()
		case Refresh =>
			studentResponses = Map()
			updateListeners()
	 	case _ =>
	}
	override def createUpdate = {
		val responses = studentResponses.values.toList
		ResponseCloudUpdate(weightTopXWords(responses, selectAmount))
	}

	/* adds up all the word counts from a list of WordCounters */
	/* then selects the top 'x' words, then weights them by frequency */
	def weightTopXWords(texts: List[WordCounter], x: Int): List[(String, Float)] = {
		var map: Map[String, Int] = Map()
		var totalWords: Int = 0
		texts.foreach(t => 
			for((word, count) <- t.wordCount)
			yield {
				map.get(word) match {
					case Some(n) => map += word -> (n+count)
					case None => map += word -> count
				}
				totalWords += count
			}
		)
		val selectedWords = map.toList.sortWith((a, b) => a._2 > b._2).take(x)
		val totalCount = selectedWords.foldLeft("", 0)((a, b) => 
			("", a._2 + b._2))._2
		selectedWords.map(x => (x._1, x._2.toFloat/totalCount))
	}
}

/* 
 * Little helper class
 * Instantiated w/ a student's response text string
 * Keeps a word count so you don't have to recount every time
 */
class WordCounter(str: String) {
	var wordCount: Map[String, Int] = doCount(str)

	def update(s: String) = {wordCount = doCount(s)}
	def doCount(s: String) = countWords(splitText(s))
	
	/* splits some text into an array of its words */
	private def splitText(str: String) = {
		str.toLowerCase
			.replaceAll("[^ &&[\\W&&[^']]]", "")
			.split("\\s+")
			.filterNot(StopList.set.contains(_))
	}

	/* counts the frequencies within a word array */
	private def countWords(words: Array[String]): Map[String, Int] = {
		var map: Map[String, Int] = Map()
		words.foreach(w =>
			map.get(w) match {
				case Some(n) => map += w -> (n+1)
				case None => map += w -> 1
			})
		map
	}
}

/* Message that gets created and sent to the listeners */
case class ResponseCloudUpdate(weightedWords: List[(String, Float)])
/* Student id and text from JsonQuestionHandler's javascript call in the student response form */
case class LiveUpdate(id: Long, text: String)
/* Clears data, message sent from Question.stopQuestion */
case class Refresh()
