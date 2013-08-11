package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{User, Session, Section, Click}
import net.liftweb.http._
import scala.xml._
import js._
import JsCmds._
import JE._
import net.liftweb.mapper.By
import net.liftweb.mapper.By_>=

import code.model.Student

class SectionIndex extends Loggable {
	var sections = Section.findAll(By(Section.teacher, User.currentUser))

	def list = {
		/* Starting a session is duplicate code here and SectionView
		 * Should probably move it to the Section object or something */
		def startS(id: Long, title: String): JsCmd = {
			if(title != "null") {
				//clear any sessions
				logger.debug("Starting Class Session init kill, LiftSession:"+S.session.map(_.uniqueId))
				User.killAllSessionsBoxed(User.currentUser)
				logger.debug("Starting Class Session, LiftSession:"+S.session.map(_.uniqueId))
				val t = if(title != "") title else "untitled"
				val s = Session.create.section(id).date(new Date).title(t)
				s.save
				Session.addSession(s)
				User.storeSession(s)
				Click.click("Section Index", "START_SESSION", title)
				RedirectTo("/session/running?s=%d".format(s.primaryKeyField.is))
			} else Noop
		}
		def stopS(): JsCmd = {
			User.killAllSessionsBoxed(User.currentUser)
			User.clearSession(true)
			Click.click("Section Index", "STOP_SESSION", "")
			RedirectTo("/section/")
		}
		".section" #> sections.map ({ f =>
			def runButton(id: Long) = {(
				if(User.getSession.isEmpty)
					SHtml.ajaxButton(<span>Start Session</span>, SHtml.ajaxCall(JsRaw("prompt('Session Title', 'untitled')"), startS(id, _))._2, (s:String) => Noop)
				else if(User.getSession.map(_.section == id) openOr false)
					SHtml.ajaxButton(<span>Stop Session</span>, SHtml.ajaxCall(JsNull, (String) => stopS)._2, (s: String) => Noop)
				else
					<span></span>	
			)}
			def runText = { 
				if(User.getSession.map(_.section == f) openOr false) "Stop Session"
				else "Start Session"
			}		
			def deleteSection(): JsCmd = {
				Confirm("Really Delete?", RedirectTo("/section/delete?id=%s".format(f.primaryKeyField.toString)))
			}
			".fTitle [href]" #> "/section/view/%s".format(f.primaryKeyField.toString) & 
			".fTitle *" #> f.title.is &
			".sRun" #> runButton(f.primaryKeyField) &
			".fDelete " #> (
				//if(f.students.isEmpty & f.lessons.isEmpty) {
				if(f.students.isEmpty) {
					SHtml.ajaxButton("Delete",() => deleteSection)
				} else {
					<span>Delete all Students first</span>
				})
		})
	}

	/* This is developer code for when I had to encrypt the db */
	/*
	def encrypt = {
		def decryptAll = {
			Student.findAll().map(s => {
				//s.userId(StringCrypto.decrypt(s.lastName))
				//s.save
			})
			Alert("It is done!")
		}
		def encryptAll = {
			Student.findAll().map(s => {
				val first = s.firstName.is.toLowerCase
				val last = s.lastName.is.toLowerCase
				val newId = first.substring(0,1) + last
				s.userId(newId)
				s.save
			})
			Alert("It is done!")
		}
		"#encrypt_button" #> SHtml.ajaxButton("ENCRYPT", () => encryptAll) &
		"#decrypt_button" #> SHtml.ajaxButton("DECRYPT", () => decryptAll)
	}
	*/
	def create = {
		var fTitle = ""
		def createSection(): Unit = {
			if(fTitle.trim.length > 0) {
				Section.create
					.title(fTitle).teacher(User.currentUser)
					.save
				Click.click("Section Index", "CREATE_SECTION", fTitle)
				S.redirectTo("/section/")
			}
		}
		"#createF_title" #> SHtml.text("", fTitle = _, "size" -> "20") &
		"#createF_submit" #> SHtml.submit("Create Class", createSection)
	}
}
