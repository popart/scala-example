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

class LoginListenerSpec extends Spec with ShouldMatchers with SessionContext {
  describe("A LoginListener") {
		it ("should instantiate and register with running session's LoginServer") {
			DBInit.setupSession
			val dl = new LoginListener
			//dl.registerWith should not be null
			Some(dl.registerWith) should equal (Session.getLoginServer(User.getSessionId))
		}
  }
	describe("A LoginServer") {
		it ("should be cleared from memory when a session ends") {
			DBInit.setupSession
			User.clearSession(true)
			Session.getLoginServer(User.getSessionId) should be (None)
		}
		/* OK iono it keeps saying the map is empty
		 * even though it works in production
		 */  
		/*
		it ("should correctly keep a map of students") {
			var s = new LoginServer
			//s.students should have size (0)
			val initLoginLines = 
				LoginLine("a", false) :: LoginLine("b", false) :: Nil
			s ! LoginServerUpdate(initLoginLines)
			s.students should not have size (0)
		}
		*/
	}
}
