package code 
package snippet 

import net.liftweb.mapper.{By, NotNullRef}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Question, Session, User}
import code.comet.QuestionServer
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JsCmds._
import scala.xml.XML

/*
 * Just displays the question
 */
class QuestionAdmin {
	val questionServer = Session.getQuestionServer(User.getSessionId)
	def questionText = questionServer.map(_.getText) getOrElse "Waiting for Question"
	def img = questionServer.map(_.getImg)
	def render = {
		"#result" #> (try {XML.loadString("<span>"+questionText+"</span>")} catch {
			case _ => <span>{questionText}</span> }) &
		"#q_img" #> img
	}
}
