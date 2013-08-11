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
import js._
import JsCmds._
import js.JE.JsRaw
import code.comet.{QuestionServer, ResponseServer, ResponseServerUpdate, AskQ, DisplayServer, DisplayServerUpdate}

/*
 * renders some ajax stuff to stop an asked question
 * can js close the question window b/c it was opened in a new window/tab
 * w/ javascript
 */
class StopQuestion extends Loggable{
	def render = {
		def stopQuestion(): JsCmd = {
			if(Question.stop()) Click.click("Question Admin", "STOP_QUESTION", "")
			else Click.click("Question Admin", "STOP_QUESTION", "FAIL")
			Run("self.close()")
		}
		"#stop_btn" #> SHtml.ajaxSubmit("Close Question", stopQuestion)
	}
}
