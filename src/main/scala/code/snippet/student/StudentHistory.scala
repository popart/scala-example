package code
package snippet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import code.model.{Student, Session, User, Response}
import net.liftweb.mapper.By
import java.text.DateFormat
import net.liftweb.sitemap._

case class StudentId(id: String)
class StudentHistory(si: StudentId) {
	def student = Student.findByKey(si.id.toLong) openOr Student.create
	val df:DateFormat = DateFormat.getDateInstance
	def render = {
		"#student_name" #> student.firstName.is &
		".response" #> student.responses.map(r => {
			".question_txt *" #> r.question.map(_.text.is) &
			".date *" #> df.format(r.date.is) &
			".response_txt *" #> r.text.is &
			".response_score *" #> r.score.is
		})

	}
}

object StudentHistoryParam {
	val menu = Menu.param[StudentId]("Student History", "Student History",
		s => Full(StudentId(s)),
		si => si.id) / "student" / "history"
	lazy val loc = menu.toLoc
	def render = "*" #> loc.currentValue.map(_.id)
}
