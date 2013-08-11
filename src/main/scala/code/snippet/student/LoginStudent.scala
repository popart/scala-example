package code
package snippet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import java.util.Date
import code.model.{Student, Session, User}
import net.liftweb.mapper.By

//todo:still needs error checking for params
object StudentLogin extends Logger {
	def render = {
		var loginId = ""
		var password = ""

		def findStudentByLoginId(loginId:String): Box[Student] = {
			//encrypt
			Student.find(By(Student.userId, StringCrypto.encrypt(loginId)))	
		}
		def logStudentIn(student:Student) = {
			Student.login(student)
		}
		def loginStudent() = {
			findStudentByLoginId(loginId) match {
				case Full(student) if (student.validated_? &&
					student.testPassword(password)) => {
						debug("Student:{"+student.primaryKeyField.toString+
							", "+student.lastName.is+
							"} found class session and validated password")
						logStudentIn(student)
						S.redirectTo("/?s=%s".format(student.primaryKeyField.toString+(new Date).getTime))
					}
				case Full(student) if !student.validated_? => 
					debug("Student:{"+student.primaryKeyField.toString+
							", "+student.lastName.is+
							"} faild login - no running class session")
					S.error("Sorry %s, your class is not in session".format(student.firstName))
				case _ => {
					debug("Invalid student login:{"+
						loginId+", "+password+"}")	
					S.error("Invalid username and password")
				}
			}
		}
		"#userId " #> SHtml.text("", loginId = _) &
		"#password " #> SHtml.password("", password = _ ) &
		"#login_submit " #> SHtml.submit("Login", loginStudent)
		}
}
