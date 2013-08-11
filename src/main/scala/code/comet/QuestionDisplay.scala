package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import SHtml._
import net.liftweb.actor._
import net.liftweb.util._
import TimeHelpers.seconds
import code.model.{Student, Question, Response, User, Session}
import net.liftweb.http.js.JsCmd
import js.JsCmds._
import scala.xml.XML
import java.util.Date

class QuestionDisplay extends CometActor with CometListener {
	override def lifespan = Full(seconds(60))
	private var question:Box[Question] = Empty
	private var questionText = ""
	private var img: Box[String] = Empty
	//private var questionId = -1L
	//private var responseText = ""

	def registerWith = 
		Session.getQuestionServer(Student.getSessionId) getOrElse (
		Session.getQuestionServer(User.getSessionId) getOrElse
		new QuestionServer)
	
	override def lowPriority = {
		case AskQ(text, img_link, id, open_?, pop_?) => {
			questionText = if(text != "") text else "Waiting for Question"
			img = img_link
			//questionId = id
			//responseText = ""
			reRender
		}
	}

	override def render = {
		"#question_text" #> (try {XML.loadString("<span>"+questionText+"</span>")} catch {
			case _ => <span>{questionText}</span>
		})&
		"#question_img" #> (if(img.isDefined)
			<img src={img.open_!} id="question_img"/>
			else <span></span>)
	}
}
