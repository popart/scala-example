package code 
package snippet 

import net.liftweb.mapper.{By, NullRef}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Click, Section, Student, Session, User}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JE._
import JsCmds._
import js.jquery.JqJsCmds._

case class SectionId(theId:String)

class SectionView(si:SectionId) {
	val daSection = Section.findByKey(si.theId.toLong) openOr Section.create
	val students = daSection.students.toList.sortWith((a, b) => 
		a.displayName < b.displayName)
	def render = "#sTitle" #> daSection.title.is
	def lsStudents = {
		var firstName = ""
		var lastName = ""
		var userId = ""
		var password = ""
		var lastNameField = Map[String, String](); var firstNameField = Map[String, String]();
		var userIdField = Map[String, String](); var passwordField = Map[String, String]();
		
		def flip(rid: String) = {
			Click.click("Section View", "EDIT", rid)
			Hide("edit_"+rid) & Show("save_"+rid) &
			Hide("last_"+rid) & Show("last_field_"+rid) &
			Hide("first_"+rid) & Show("first_field_"+rid) &
			Hide("userId_"+rid) & Show("userId_field_"+rid) &
			Hide("password_"+rid) & Show("password_field_"+rid)
		}
		def createS(): JsCmd = {
			if(firstName.trim.length == 0 || lastName.trim.length == 0 || userId.trim.length == 0 || password.trim.length == 0)
				S.error("student_msg", "New Student requires first and last names")
			else {
				var s = Student.create
				s.section(daSection)
				s.firstName(firstName).lastName(lastName).userId(userId).password(password)
				s.validate match {
					case Nil => {
						Click.click("Section View", "CREATE_STUDENT", lastName)
						s.save
						RedirectTo("/section/view/%s".format(si.theId))
					}
					case errors:List[FieldError] => {
						Click.click("Section View", "CREATE_STUDENT", "FAILED")
						S.error(errors)
						Noop
					}
					case _ => Noop
				}
			}
		}
		".student *" #> students.map({s =>
			val rowId=s.primaryKeyField.toString
			def unflip(rid: String) = {
				SetHtml("last_"+rid, <span id={"last_"+rid}>{s.lastName.is}</span>) &
				SetHtml("first_"+rid, <span id={"first_"+rid}>{s.firstName.is}</span>) &
				SetHtml("userId_"+rid, <span id={"userId_"+rid}>{s.userId.is}</span>) &
				SetHtml("password_"+rid, <span id={"password_"+rid}>{s.password.is}</span>) &
				Hide("last_field_"+rid) & Show("last_"+rid) &
				Hide("first_field_"+rid) & Show("first_"+rid) &
				Hide("userId_field_"+rid) & Show("userId_"+rid) &
				Hide("password_field_"+rid) & Show("password_"+rid) &
				Hide("save_"+rid) & Show("edit_"+rid)
			}
			def saveStudent(rid: String): JsCmd = {
				if(firstNameField(rid).trim.length == 0 || lastNameField(rid).trim.length == 0 || userIdField(rid).trim.length == 0 || passwordField(rid).trim.length == 0) {
					S.error("student_msg", "Student requires all Fields")
					Noop
				} else {
					s.lastName(lastNameField(rid)).firstName(firstNameField(rid)).userId(userIdField(rid)).password(passwordField(rid))
					s.validate match {
						case Nil => {
							s.save
							Click.click("Section View", "SAVE_STUDENT", "")
							unflip(rid)
						}
						case errors:List[FieldError] => {
							Click.click("Section View", "SAVE_STUDENT", "FAIL")
							S.error(errors)
							Noop
						}
					}
				}
			}
			".rowForm" #> SHtml.ajaxForm(
				(<td><span id={"last_"+rowId}>{s.lastName.is}</span>
						<span id={"last_field_"+rowId} style="display:none">{SHtml.text(s.lastName.is, lastNameField += rowId -> _, "class" -> "span-3")}</span>
				</td>
				<td><span id={"first_"+rowId}>{s.firstName.is}</span>
						<span id={"first_field_"+rowId} style="display:none">{SHtml.text(s.firstName.is, firstNameField += rowId -> _, "class" -> "span-3")}</span>
				</td>
				<td><span id={"userId_"+rowId}>{s.userId.is}</span>
						<span id={"userId_field_"+rowId} style="display:none">{SHtml.text(s.userId.is, userIdField += rowId -> _, "class" -> "span-3")}</span>
				</td>
				<td><span id={"password_"+rowId}>{s.password.is}</span>
						<span id={"password_field_"+rowId} style="display:none">{SHtml.text(s.password.is, passwordField += rowId -> _, "class" -> "span-3")}</span>
				</td>
				<td>
					<a href={"/student/history/"+s.primaryKeyField.toString}>History</a>
				</td>
				<td><span class="span-4">
					<span id={"edit_"+rowId}>{SHtml.ajaxButton("Edit", () => flip(rowId))}</span>
					<span id={"save_"+rowId} style="display:none">{SHtml.ajaxSubmit("Save", () => {saveStudent(rowId)})}</span>
					{SHtml.ajaxButton("Delete", () => {
							Confirm("Really Delete?", RedirectTo("/student/delete?id=%s&section=%s".format(s.primaryKeyField.toString, si.theId)))
					})}
				</span></td>).toSeq
			)
		}) &
		//create
		"#newStudentForm" #> SHtml.ajaxForm(
			<td>{SHtml.text("", lastName = _, "class" -> "span-3")}</td>
			<td>{SHtml.text("", firstName = _, "class" -> "span-3")}</td>
			<td>{SHtml.text("", userId = _, "class" -> "span-3")}</td>
			<td>{SHtml.text("", password = _, "class" -> "span-3")}</td>
			<td></td>
			<td>{SHtml.ajaxSubmit("New Student", createS)}</td>
		)
	}
	
	def startSession= {
		/* Needs to stay in line w/ SectionIndex */
		def startS(id: Long, title: String): JsCmd = {
			if(title != "null") {//js prompt cancel returns "null"
				//clear any sessions
				User.killAllSessionsBoxed(User.currentUser)
				val t = if(title != "") title else "untitled"
				val s = Session.create.section(id).date(new Date).title(t)
				s.save
				Session.addSession(s)
				User.storeSession(s)
				Click.click("Section View", "START_SESSION", title)
				RedirectTo("/session/running?s=%d".format(s.primaryKeyField.is))
			} else Noop
		}
		def stopS() = {
			User.killAllSessionsBoxed(User.currentUser)
			User.clearSession(true)
			Click.click("Section View", "STOP_SESSION", "")
			RedirectTo("/section/")
		}
		def runButton(id: Long) = {(
			if(User.getSession.isEmpty)
				SHtml.ajaxButton(<span>Start Session</span>, SHtml.ajaxCall(JsRaw("prompt('Session Title', 'untitled')"), startS(id, _))._2, (s:String) => Noop)
			else if(User.getSession.map(_.section == id) openOr false)
				SHtml.ajaxButton(<span>Stop Session</span>, SHtml.ajaxCall(JsNull, (String) => stopS)._2, (s: String) => Noop)
			else
				<span></span>	
		)}
		"#sTitle" #> daSection.title.is &
		"#session_start" #> runButton(daSection.primaryKeyField)
	}
}

object SectionViewParam {
	val menu = Menu.param[SectionId]("Section View", "Section View",
																	s => Full(SectionId(s)),
																	fi => fi.theId) / "section" / "view"
	lazy val loc = menu.toLoc
	def render = "*" #> loc.currentValue.map(_.theId)
}
