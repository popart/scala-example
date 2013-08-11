package code
package snippet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import code.model.{Click, Section}

//todo:still needs error checking for params
object SectionDelete {
	def render = {
		S.param("id") match {
			case Full(id: String) => {
				val section = Section.findByKey(id.toLong)
				if(section.isDefined) {
					section.open_!.delete_!
					Click.click("Section Index", "DELETE_SECTION", "")
				} else S.error("Could not find Section")
				S.redirectTo("/section/")
			}
			case _ => {
				S.error("Invalid parameters")
				S.redirectTo("/section/")
			}
		}
	}
}
