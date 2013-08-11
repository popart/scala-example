package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{User, Session}
import net.liftweb.http._
import scala.xml._
import js._
import JsCmds._
import JE._
import code.model.{User, Session, Click}
import code.comet.{LoginServer, LoginServerUpdate, LoginLine}

/*
 * The display for the current running class session
 */
class SessionInfo {
	def render = {
		def curSession = User.getSession openOr Session.create
		def students = curSession.loggedInStudents
		def allStudents = curSession.section.map(_.students.toList) openOr Nil
		def loginServer = Session.getLoginServer(User.getSessionId) getOrElse null
		Session.getLoginServer(User.getSessionId).map(_ ! LoginServerUpdate(allStudents.map(s => LoginLine(s.displayName, students.contains(s)))))
		"#session_title" #> curSession.title.is &
		"#section_name " #> (curSession.section.map(_.title.is) openOr "No Class Name") &
		"#date" #> curSession.date.is.toString &
		"#stop_btn" #> SHtml.ajaxButton("Stop Session", () => stopSession)
	}
	//copied from snippet.SectionIndex and snippet.SectionView
	//Maybe move to User?
	def stopSession(): JsCmd = {
		User.killAllSessionsBoxed(User.currentUser)
		User.clearSession(true)
		Click.click("Class in Session", "STOP_SESSION", "")
		RedirectTo("/section/")
	}
}
