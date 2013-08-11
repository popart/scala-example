package code
package snippet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import code.model.{Student, Session, User}
import net.liftweb.mapper.By
import java.text.DateFormat

//todo:still needs error checking for params
class StudentJournal {
	def student = Student.currentStudent openOr Student.create.firstName("Teacher").lastName("Teacher")
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
