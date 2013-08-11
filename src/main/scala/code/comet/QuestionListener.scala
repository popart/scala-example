package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.mapper.By
import net.liftweb.util.TimeHelpers.seconds
import SHtml._
import net.liftweb.actor._
import net.liftweb.util._
import code.model.{Student, Question, Response, User, Session}
import net.liftweb.http.js.JsCmd
import js.JsCmds._
import js.jquery.JqJsCmds._
import js.JE._
import scala.xml._
import java.util.Date
import code.snippet.QuestionResponse

class QuestionListener extends CometActor with CometListener with Loggable {
	override def lifespan = Full(seconds(60))
	private var student:Box[Student] = Empty
	private var question = <span>"Waiting for Question"</span>
	private var img: Box[String] = Empty
	private var questionId: Long = -1
	//private var open: Boolean = false

	def server = {
		val servah = Session.getQuestionServer(Student.getSessionId) 
		servah match{
		 case Some(x) => servah 
		 case None => Session.getQuestionServer(User.getSessionId)
		}
	}

	def registerWith = server getOrElse (new QuestionServer)
	override def localShutdown = {
		logger.trace("Shutting down a QuestionListener")
		super.localShutdown
	}
	override def localSetup = {
		logger.trace("Starting up a QuestionListener")
		super.localSetup
	}

	override def lowPriority = {
		case AskQ(text, img_link, id, open_?, popUpdate_?) => {
			question = if(text != "") {
				try {XML.loadString("<span>"+text+"</span>")} catch {
					case e:Exception => <span>{text}</span>
				}} else <span>"Waiting for Question"</span>
			img = img_link
			questionId = id
			//open = open_?
			//clear response text
			if(!popUpdate_?) QuestionResponse.response.remove()
			//SetHtml inserts html inside tags
			if(open_? & text=="LOGOUT") partialUpdate(RedirectTo("/student/logout"))
			else if(open_? & !popUpdate_?) partialUpdate(
				SetValById("response_area", Str("")) & 
				SetValById("submitted_txt", Str("")) &
				//SetHtml("test_q", <span>{question}</span>) & 
				SetHtml("test_q", question) & 
				SetHtml("q_img", server.map(_.getImg) getOrElse <span></span>) &
				Show("test_q") & Show("q_img") & 
				Show("response_area") & Show("response_btn") & 
				Show("response_form") & Hide("edit_form")
			) 
			else if(open_? & popUpdate_?) partialUpdate(
				SetHtml("test_q", question)
			) 
			else partialUpdate(
				SetHtml("test_q", <span>Waiting for Question</span>) & 
				Hide("response_form") & Hide("q_img") & Hide("edit_form")
			)
		}
	}

	override def render = {
		new RenderOut(<span></span>)
		/*
		def createResponse(): JsCmd = {
			var r = Response.create
				.date(new Date)
				.session(User.currentSession.map(_.primaryKeyField.is) openOr -1L)
				.question(questionId)
				.student(Student.currentStudent.map(_.primaryKeyField.is) openOr -1L)
				.text(responseText)
			r.save
			
			ResponseServer ! ResponseLine((Student.currentStudent.map(_.displayName) openOr "Teacher Response"), responseText, true)
			reRender
			SetExp(JsVar("submitted_txt"), Str(responseText))
		}
		*/
		/*
		"#question_text" #> XML.loadString("<span>"+question+"</span>") &
		"#question_img" #> (if(img.isDefined && open)
			<img src={img.open_!} id="question_img"/>
			else <span></span>)*/ /*& 
		"#response_form" #> (if(open) {
			ajaxForm(
				textarea(responseText, responseText = _, "onkeyup" -> "checkChanges(this.value)") ++ <br/>.toSeq ++
				submit((if(fresh) "Answer" else "Change Answer"), createResponse) ++ 
				hidden(() => createResponse))
			} else <span></span>) &
		"#submit_msg *" #> responseText &
		"#submit_msg [showDialog]" #> (if(!fresh) "true" else "false") &
		"#script_test" #> <script type="text/javascript">alert('dang')</script>*/
	}
}
