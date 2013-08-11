package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import net.liftweb.util.TimeHelpers.seconds
import scala.xml.NodeSeq
import js.JsCmd
import js.JsCmds.{SetHtml, Noop, Script}
import js.jquery.JqJsCmds.AppendHtml
import code.model.{User, Session, Click}

class ResponseListener extends CometActor with CometListener {
	var studentLog: List[ResponseLine] = Nil
	def registerWith = Session.getResponseServer(User.getSessionId) getOrElse 
		(new ResponseServer)
	override def lifespan = Full(seconds(60))
	def submittedCount = studentLog.count(s => s.submitted)
	//no null protection
	def totalCount = Session.getLoginServer(User.getSessionId).map(_.students.count(s => s._2)) getOrElse 0

	def displayResponse(r: String): JsCmd = {
		Click.click("Question Admin", "DISPLAY_RESPONSE", "")
		Session.getDisplayServer(User.getSessionId).map(_ ! DisplayLine(r, true))
		Noop
	}

	override def lowPriority = {
		case ResponseServerUpdate(l) =>
			studentLog = l.sortWith((a, b) => a.order > b.order).filter(_.submitted)
			partialUpdate(
				SetHtml("responses_received", NodeSeq.Empty) &
				SetHtml("submitted_count", <span>{submittedCount}</span>) &
				SetHtml("total_count", <span>{totalCount}</span>) &
				AppendHtml("responses_received", 
					studentLog.map(u => {
						<tr>
							<td><span>{u.order}</span></td>
							<td><span>{u.response}</span>
								<div style="display:none">
									<div id={"response_"+u.order} class="myFancyBox">
										<h2>{u.response}</h2>
									</div>
								</div>
							</td>
							<td><a href={"#response_"+u.order} class="myFancyBoxLink">Show</a></td>
						</tr>
				})) &
				AppendHtml("responses_received", Script(js.JE.Call("addFancyBox").cmd))
			)
			/* deprecated QuestionDisplay function */
				/*	<td width="20%">{SHtml.ajaxButton("Display", () => displayResponse(u.response))}</td> */
		case _ =>
	}

	override def render = {
		S.appendJs(js.JE.Call("addFancyBox").cmd)
		"#submitted_count *" #> submittedCount &
		"#total_count *" #> totalCount &
		".student" #> studentLog.map(s => {
			".response_txt *" #> s.response &
			".response_num *" #> s.order &
			".myFancyBox [id]" #> ("response_"+s.order) &
			".myFancyBox *" #> <h1>{s.response}</h1> &
			".myFancyBoxLink [href]" #> ("#response_"+s.order) // &
			/* Deprecated QuestionDisplay button */
			//".display_btn" #> SHtml.ajaxButton("Display", () => displayResponse(s.response))
		})
	}
}
