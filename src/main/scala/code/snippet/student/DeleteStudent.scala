package code
package snippet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import code.model.{Student}

//todo:still needs error checking for params
object StudentDelete {
	def render = {
		S.param("id") match {
			case Full(id: String) => {
				val student = Student.findByKey(id.toLong)
				if(student.isDefined) student.open_!.delete_!
					else S.error("Could not find Student")
				if(S.param("section").isDefined) S.redirectTo("/section/view/"+S.param("section").open_!)
					else S.redirectTo("/")
			}
			case _ => {
				S.error("Invalid parameters")
				S.redirectTo("/")
			}
		}
	}
}
