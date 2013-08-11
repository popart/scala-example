package code
package snippet

import net.liftweb.http._
import net.liftweb.http.rest._

import code.model.Student

object StudentLogout {// extends RestHelper {
	def render = {
		Student.logout()
		S.error("Class has ended")
		S.redirectTo("/student/login")
	}
	/*
	serve {
		//case Req("student" :: "logout" :: Nil, GetRequest) => RedirectResponse("/banana")
		case Req("student" :: "logout" :: Nil, "", GetRequest) => {
			Student.logout()
			RedirectResponse("/")
		}
	}
	*/
}
