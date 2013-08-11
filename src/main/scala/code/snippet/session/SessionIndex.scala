package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import java.text.DateFormat
import code.lib._
import Helpers._
import net.liftweb.http._
import scala.xml._
import js._
import JsCmds._
import JE._
import net.liftweb.mapper._
import java.text.SimpleDateFormat
import java.util.{Date, Calendar}
import code.model.{Click, Session, Section, User}

/*
 * Search Class Session Histories
 * This is a stateful snippet because we need to keep the query params
 * throughout multiple paginated page loads
 */
class SessionIndex extends StatefulSnippet with StatefulSortedPaginatorSnippet[Session, MappedField[_, Session]] {
	val dispatch: DispatchIt = { 
		case "list" => list
		case "search" => search
		case "paginate" => paginate
	}
	override def count = Session.count(qParams:_*)
	override def itemsPerPage = 15
	override def headers:List[(String, MappedField[_, Session])] = {
		"title" -> Session.title :: "date" -> Session.date :: Nil	
	}

	override def page = {
		Session.findAll(
			(Seq[QueryParam[Session]](StartAt(curPage*itemsPerPage), 
			MaxRows(itemsPerPage)) ++
			qParams :+ orderClause):_*
		) //, 
	}
	/*
	override def zoomedPages = {
		(curPage-3 to curPage+3).toList filter {
			n => n >= 0 && n < numPages
		}
	}
	*/
	def qParams = {
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
			//static user filter
			In(Session.section, Section.id, By(Section.teacher, User.currentUser)),
			(if(startCal != null) By_>(Session.date, startCal.getTime) else null),
			(if(endCal != null) By_<(Session.date, endCal.getTime) else null),
			(if(sectionFilter != "0") By(Session.section, sectionFilter.toLong) else null),
			if(titleSearchStr.trim.length > 0) Like(Session.title, "%"+titleSearchStr+"%") else null
		)
	}
	def orderClause = {
		OrderBy(Session.date, Descending)
	}
	val df:DateFormat = DateFormat.getDateInstance
	var sessions = Session.findAll(OrderBy(Session.date, Descending))
	private var titleSearchStr = ""
	private val dateFormatter = new SimpleDateFormat("MM/dd/yyyy")
	private var startDateStr = ""
	private var endDateStr = ""
	private var sectionFilter = "0"
	private val sectionOpts = ("0", "All Classes") :: Section.findAll(By(Section.teacher, User.currentUser)).map(s => (s.primaryKeyField.toString, s.title.is))
	
	def search = {
		def searchIt() =  {
			Click.click("Session Histories", "SEARCH", "")
			this.redirectTo("/session/")		
		}
		"#startDate *" #> SHtml.text(startDateStr, startDateStr = _, "size" -> "7") &		
		"#endDate *" #> SHtml.text(endDateStr, endDateStr = _, "size" -> "7") &		
		"#search_box" #> SHtml.text(titleSearchStr, titleSearchStr = _) &
		"#classSelect" #> SHtml.select(
			sectionOpts,
			Full(sectionFilter),
			sectionFilter = _) &
		"#search_submit" #> SHtml.submit("Search", searchIt)
	}

	def list = {
		".session" #> page.map ({ f =>
			".fTitle [href]" #> "/session/view/%s".format(f.primaryKeyField.toString) & 
			".fTitle *" #> (if(f.title.is != null) f.title.is else "untitled") &
			".fDate *" #> <span>{df.format(f.date.is)}</span> &
			".fSection *" #> (f.section.map(_.title.is) openOr "Blank Section")
		})
	}
}
