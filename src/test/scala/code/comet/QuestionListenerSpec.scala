package code
package snippet

import code.model._
import code.comet._
import net.liftweb.http._
import net.liftweb.mapper.BaseMetaMapper
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import net.liftweb.mapper._
import Helpers._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.xml._

class QuestionListenerSpec extends Spec with ShouldMatchers with SessionContext {
  describe("A QuestionListener") {
		it ("should instantiate and register with running session's QuestionServer") {
			DBInit.setupSession
			val dl = new QuestionListener
			//dl.registerWith should not be null
			Some(dl.registerWith) should equal (Session.getQuestionServer(User.getSessionId))
		}
  }
	describe("A QuestionServer") {
		it ("should be cleared from memory when a session ends") {
			DBInit.setupSession
			User.clearSession(true)
			Session.getQuestionServer(User.getSessionId) should be (None)
		}
	}
}
