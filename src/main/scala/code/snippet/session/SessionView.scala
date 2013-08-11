package code 
package snippet 

import net.liftweb.mapper._
import scala.xml._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.db.DB
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Click, Response, Folder, Session, User}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import js.JE.{Call, Str}
import JsCmds._
import SHtml._
import code.comet.{ResponseServer, ResponseServerUpdate, ResponseLine}
import java.text.DateFormat

case class SessionId(theId: String)

/*
 * List all the questions asked in a session
 * and their responses
 * along with an ajax grade form (some radio buttons)
 */
class SessionView(fi: SessionId) {
	var daSession = Session.findByKey(fi.theId.toLong)
	var questions = daSession.map(_.questions.toList) openOr Nil
	def render = {
		val df:DateFormat = DateFormat.getDateInstance
		"#session_title" #> (daSession.map(_.title.is) openOr "untitled") &
		"#class_name" #> (daSession.map(_.section.map(_.title.is) openOr "No class name data") openOr "Unnamed Class") &
		"#session_date" #> (daSession.map(s => df.format(s.date.is)) openOr "No Date info") &
		".question *" #> {questions.map(q => {
			def text = q.text.is
			//if you change the ordering hiding edited responses under the final
			//will break
			val responses = Response.findAllByPreparedStatement({superconn =>
				superconn.connection.prepareStatement(
					"select * from response r" +
					" inner join student s on s.id = r.student" +
					" where r.question = "+q.primaryKeyField.toString+
					" and r.session_c = "+(daSession.map(_.primaryKeyField.toString) openOr "-1") +
					" order by s.lastname, s.firstname, r.date_c desc")
			})
			".result *" #> (try {XML.loadString("<span>"+text+"</span>")} catch {
				case _ => <span>{text}</span>
			}) &
			//".response *" #> {responses.map(r => {
			".response" #> {responses.map(r => {
				val uneek = SecurityHelpers.randomString(10)
				def grade(s: Int): JsCmd = {
					Click.click("Session History View", "GRADE_RESPONSE", "")
					r.score(s).save 
					Noop
				}
				".response_td [qid]" #> (r.question.toString) &
				".student_name *" #> <a href={"/student/history/%s".format(r.student.map(_.primaryKeyField.toString) openOr "0")}>{r.student.map(_.displayName) openOr "No name"}</a> &
				".r_text *" #> r.text.is &
				".myFancyBox *" #> <h1>{r.text.is}</h1> &
				".myFancyBox [id]" #> ("response_"+uneek) &
				".myFancyBoxLink [href]" #> ("#response_"+uneek) &
				".grade_form *" #> starForm(ajaxRadio(List(1, 2, 3, 4), Full(r.score.is), grade /*, "class" -> "star"*/))
			})}
		})}
	}
	private def starForm(choices: ChoiceHolder[_]): NodeSeq = {
		choices.flatMap(c => <span>{c.xhtml}{c.key.toString}</span>)
	}
	/*
	private class ApplicableElem(in: Elem) {
		def %(attr: ElemAttr): Elem = attr.apply(in)
	}
	private implicit def elemToApplicable(e: Elem): ApplicableElem = 
		new ApplicableElem(e)
	private def checked(in: Boolean) = if(in) new UnprefixedAttribute("checked", "checked", Null) else Null
	private def myAjaxRadio[T](opts: Seq[T], deflt: Box[T], ajaxFunc: T => JsCmd, attrs: ElemAttr*): ChoiceHolder[T] = {
		val groupName = Helpers.nextFuncName
		val itemList = opts.map{
			v => {
				ChoiceItem(v, attrs.foldLeft(<input type="radio" name={groupName}
					value={Helpers.nextFuncName}/>)(_ % _) %
					checked(deflt == Full(v)) %
					("onchange" -> ajaxCall(Str(""), ignore => ajaxFunc(v))._2.toJsCmd))
			}
		}
		ChoiceHolder(itemList)
	}
	*/
}
object SessionViewParam {
	val menu = Menu.param[SessionId]("Session History", "Session History",
																	s => Full(SessionId(s)),
																	fi => fi.theId) / "session" / "view"
	lazy val loc = menu.toLoc
	def render = "*" #> loc.currentValue.map(_.theId)
}
