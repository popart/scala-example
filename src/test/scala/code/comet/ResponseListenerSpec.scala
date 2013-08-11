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

class ResponseListenerSpec extends Spec with ShouldMatchers with SessionContext {
  describe("A ResponseListener") {
		it ("should instantiate and register with running session's ResponseServer") {
			DBInit.setupSession
			val dl = new ResponseListener
			//dl.registerWith should not be null
			Some(dl.registerWith) should equal (Session.getResponseServer(User.getSessionId))
		}
  }
	describe("A ResponseServer") {
		it ("should be cleared from memory when a session ends") {
			DBInit.setupSession
			User.clearSession(true)
			Session.getResponseServer(User.getSessionId) should be (None)
		}
	}
}
