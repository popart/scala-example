package code
package snippet

import code.model._
import net.liftweb.http._
import net.liftweb.mapper.BaseMetaMapper
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import net.liftweb.mapper._
import Helpers._
import org.scalatest.{Spec, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers
import scala.xml._

class UserTest extends Spec with ShouldMatchers with SessionContext {
	describe("Simple User Stuff") {
		it ("should get the user from the session") {
			val user = DBInit.createUser
			User.logUserIn(user)
			User.currentUser should not be Empty
		}
	}
}
