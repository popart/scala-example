package code 
package snippet 

import scala.xml._
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Student, Question, User, Response, Session}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JsCmds._
import js.jquery.JqJsCmds._
import js.JE._
import SHtml._
import code.comet.{QuestionServer, ResponseServer, ResponseServerUpdate, ResponseLine}

object QuestionResponse extends QuestionResponse {
	object response extends SessionVar[String]("")
}

/*
 * Renders an ajax form that students use to submit responses
 * Students can flip between post submission views and edit views
 */
class QuestionResponse {
	def responseText = QuestionResponse.response.is
	def questionServer = Session.getQuestionServer(Student.getSessionId)
	var modified = false

	def render = {
		S.appendJs(SetExp(JsVar("js_submitted_txt"), Str(responseText)) &
			SetExp(JsVar("student_id"), Str((Student.currentStudent.map(_.primaryKeyField.is) openOr -1L).toString)))
		def visibility = if(!(questionServer.map(_.isRunning_?) getOrElse false)) "style" -> "display:none" else "" -> ""
		def createResponse(): JsCmd = {
			if(modified) {
				var r = Response.create
					.date(new Date)
					.session(User.getSession.map(_.primaryKeyField.is) openOr -1L)
					.question(questionServer.map(_.getId) getOrElse -1L)
					.student(Student.currentStudent.map(_.primaryKeyField.is) openOr -1L)
					.text(responseText)
					.saveMe

				Session.getResponseServer(Student.getSessionId).map(_ ! ResponseLine(Student.currentStudent.map(_.primaryKeyField.toString) openOr "0", responseText, true, 0))
	
				modified = false
			}
			SetValById("response_area", responseText) &
			SetHtml("submitted_txt", <span>{responseText}</span>) &
			Show("submitted_txt") & Show("edit_form") &
			Hide("response_form") & Hide("edited_msg") &
			SetExp(JsVar("js_submitted_txt"), Str(responseText))
		}
		"#response_form *" #> 
			ajaxForm(
				textarea(responseText, rt => 
					if(!rt.trim.equals(responseText)) {
						QuestionResponse.response(rt.trim) 
						modified = true
					},
					"onkeyup" -> "checkChanges(this.value)", 
					"onkeypress" -> "checkEnter(event, this.value)", 
					"id" -> "response_area", visibility) ++ 
				(<br/><span id="edited_msg" style="color:red; display:none">Changes have not been submitted</span><br/>).toSeq ++ 
				submit("Submit Response", createResponse, "id" -> "response_btn", visibility) ++
				hidden(() => createResponse)
			) &
		"#test_q *" #> (questionServer.map(q => 
			try XML.loadString("<span>"+q.getText+"</span>") catch {
				case e: Exception => <span>{q.getText}</span>}) getOrElse 
			<span>Class has ended.  <a href="/student/logout">Logout</a></span>) &
		"#q_img *" #> (questionServer.map(_.getImg) getOrElse <span></span>)
	} 
}

