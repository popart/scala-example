package code
package snippet

import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import net.liftweb.http._
import net.liftweb.mapper._
import js._
import JsCmds._
import java.text.SimpleDateFormat
import java.util.{Date, Calendar}
import code.model.{Session, Section, User, Response, Student, Question, Click}

/*
 * Student Response search form 
 * A paginated table thanks to StatefulSnippet
 * Pretty much the same as SessionIndex
 */
class ResponseIndex extends StatefulSnippet with StatefulSortedPaginatorSnippet[Response, MappedField[_, Response]] {
	val dispatch: DispatchIt = {
		case "search" => search
		case "list" => list
		case "paginate" => paginate
	}
	override def headers:List[(String, MappedField[_, Response])] = List() 

	override def count = Response.count(qParams:_*)
	override def itemsPerPage = 15
	override def page = Response.findAll((Seq[QueryParam[Response]](
		StartAt(curPage*itemsPerPage),
		MaxRows(itemsPerPage)) ++
		qParams):_*
	)

	def qParams:Seq[QueryParam[Response]] = { 
		var startDate = try{dateFormatter.parse(startDateStr)}
			catch {case e:Exception => null}
		var endDate = try{dateFormatter.parse(endDateStr)}
			catch {case e:Exception => null}

		var startCal:Calendar = null
		var endCal:Calendar = null
		if(startDate != null) {
			startCal = Calendar.getInstance
			startCal.setTime(startDate)
			startCal.add(Calendar.DATE, -1)
		}
		if(endDate != null) {
			endCal = Calendar.getInstance
			endCal.setTime(endDate)
			endCal.add(Calendar.DATE, 1)
		}

		Seq(
			//static order
			OrderBy(Response.date, Ascending),
			//static user filter
			In(Response.session, Session.id, 
				In(Session.section, Section.id, By(Section.teacher, User.currentUser))),
			//form params
			(if(startCal != null) By_>(Response.date, startCal.getTime) else null),
			(if(endCal != null) By_<(Response.date, endCal.getTime) else null),
			(if(sectionFilter != "0") In(Response.student, Student.id,
				By(Student.section, sectionFilter.toLong)) else null),
			(if(studentFilter != "0") By(Response.student, studentFilter.toLong)
				else null)
			) ++ 
			(if(questionText.trim.length > 0) questionText.split(" +").toList.map(qt => 
				In(Response.question, Question.id, Like(Question.text, "%"+qt+"%")) 
				) else Seq())
	}
	private val dateFormatter = new SimpleDateFormat("MM/dd/yyyy")
	private var startDateStr = ""
	private var endDateStr = ""
	private var sectionFilter = "0"
	private var studentFilter = "0"
	private var questionText = ""

	private val sections = Section.findAll(By(Section.teacher, User.currentUser))
	private val sectionOpts = ("0", "All Classes") :: sections.map(s => (s.primaryKeyField.toString, s.title.is))
	def studentOpts = {
		if(sectionFilter == "0") List(("0", "Select a Class"))
		else ("0", "All Students") :: (Student.findAll(By(Student.section, sectionFilter.toLong))
			.map(s => (s.primaryKeyField.toString, s.displayName)))
	}

	def search = {
		def setSection(s: String):JsCmd = {
			sectionFilter = s
			studentFilter = "0"
			Click.click("Response Search", "SET_CLASS", "")
			ReplaceOptions("studentSelect", studentOpts, Full(studentFilter)) 
		}
		def searchIt() = {
			Click.click("Response Search", "SEARCH", "")
			this.redirectTo("/response/")
		}
		"#startDate *" #> SHtml.text(startDateStr, startDateStr = _, "size" -> "7") &		
		"#endDate *" #> SHtml.text(endDateStr, endDateStr = _, "size" -> "7") &		
		"#classSelect" #> SHtml.ajaxSelect(
			sectionOpts,
			Full(sectionFilter),
			setSection(_)) &
		"#studentSelect" #> SHtml.untrustedSelect(
			studentOpts,
			Full(studentFilter),
			studentFilter = _) &
		"#questionText *" #> SHtml.text(questionText, questionText = _) &
		"#search_submit" #> SHtml.submit("Search", () => this.redirectTo("/response/"))
	}
	def list = {
		".response" #> page.map({r => 
			" [name]" #> (r.student.map(_.id.toString) openOr "0") &
			".rStudent *" #> (r.student.map(_.displayName) openOr "no name") &
			".rText *" #> r.text.is &
			".qId *" #> {
					val id = r.question.toString; val link = "/question/view/"+id;
					<a href={link}>{id}</a>
			} &
			".rDate *" #> dateFormatter.format(r.date.is)
		})
	}
}


