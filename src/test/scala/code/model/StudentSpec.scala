package code
package snippet

import code.model._
import net.liftweb.http._
import net.liftweb.mapper.BaseMetaMapper
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.mockweb.MockWeb
import code.lib._
import net.liftweb.mapper._
import Helpers._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.xml._

class StudentSpec extends Spec with ShouldMatchers with SessionContext {
	val testSession = Full(S.session)
	val testUrl = "http://seedingproject.com"

  describe("A Student") {
		it ("should not log in without a session") {
			val student = DBInit.createStudent
			Student.login(student)
			Student.currentStudent should be (Empty)
		}
		it ("should log in to a running session") {
			DBInit.setupSession
			Student.currentStudent should not be Empty
			Student.getSession should not be Empty
		}
		it ("should get all the comet servers") {
			DBInit.setupSession
			val session = Student.getSession openOr Session.create
			val sessionId = Full(session.primaryKeyField.is)
			Session.getLoginServer(sessionId) should not be null
			Session.getQuestionServer(sessionId) should not be null
			Session.getDisplayServer(sessionId) should not be null
			Session.getResponseServer(sessionId) should not be null
		}
  }
}
