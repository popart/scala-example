package code 
package snippet 

import net.liftweb.mapper.{By, NotNullRef}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Session, Folder, Question, User, Click}
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.http.js._
import JsCmds._
import js.jquery.JqJsCmds.{Show, Hide}
import js.JE.JsRaw
import code.comet.{QuestionServer, ResponseServer, ResponseServerUpdate, AskQ, DisplayServer, DisplayServerUpdate}

/*
 *A form to enter in pop question after-text
 */
class PopQuestion extends Loggable{
	var qText = ""
	def render = {
		def popQuestion(): JsCmd = {
			if(User.getSession.isDefined) {
				Session.getQuestionServer(User.getSessionId).map(_ !
					AskQ(qText, Empty, 0, true, true))
				Click.click("Pop Question", "ENTER_POP_TEXT", qText)
				SetHtml("result", <span>{qText}</span>) &
				Hide("pop_textarea") & Hide("pop_btn")
			} else {
				Click.click("Pop Question", "ENTER_POP_TEXT", "FAIL")
				Noop
			}
		}
		"#pop_textarea" #> SHtml.textarea("Enter Question", qText = _) &
		"#pop_btn" #> SHtml.ajaxSubmit("Enter", popQuestion)
	}
}

