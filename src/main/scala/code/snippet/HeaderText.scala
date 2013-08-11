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
class HeaderText {
	def firstName: String = " " + (Student.currentStudent.map(_.firstName.is) openOr  (User.currentUser.map(_.firstName.is) openOr "to SEEDING"))
	def render = {
		"#header_txt" #> ("Welcome" + firstName + "!")
	}
}
