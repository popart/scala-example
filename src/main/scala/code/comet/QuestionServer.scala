package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import scala.xml.NodeSeq
import code.model.Question

/* 
 * Displays the current question to the students and possibly other pages
 */
class QuestionServer extends LiftActor with ListenerManager {
	private var q: Box[String] = Empty
	private var img: Box[String] = Empty
	private var id: Box[Long] = Empty
	private var open: Boolean = false
	private var popUpdate: Boolean = false
	def isRunning_? = q.isDefined
	def question = q openOr Question.create
	def getText = q openOr "Waiting for Question"
	def getId = id openOr -1L
	def getImg = if(img.isDefined) <img src={img.open_!} /> else <span></span>

	override def lowPriority = {
		//ask a question
		case AskQ(text, img_link, qid, open_?, pop_?) if open_? =>
			q = Full(text)
			popUpdate = pop_?
			if(!pop_?) {
				img = img_link
		   	id = Full(qid)	
				open = true
			} else { //updates the text of pop question after teacher inputs
				Question.findByKey(id openOr 0).map(_.text(text).save)
			}
			updateListeners()
		//close a question
		case AskQ(text, img_link, qid, open_?, pop_?) if !open_? =>
			q = Empty
			img = Empty
			id = Empty
			popUpdate = false
			open = false
			updateListeners()
	 	case _ =>
	}
	def createUpdate = AskQ(q openOr "", img, id openOr -1, open, popUpdate)
}

/* open: set false to end the question (remove from student screen)
 * popUpdate: set true to simply change text and not actually ask a question
 */
case class AskQ(text: String, img: Box[String], id: Long, open: Boolean, popUpdate: Boolean)
