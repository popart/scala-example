package code
package model

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

class SessionSpec extends Spec with ShouldMatchers with SessionContext {
  describe("The Class Session object") {
		it ("should create new comet servers when a session is added") {
			val session = Session.create.saveMe
			Session.addSession(session)	
			Session.getDisplayServer(Full(session.primaryKeyField.is)) should not be (None)
			//IMPORTANT! will bleed into other tests
			Session.removeSession(session)
		}
		it ("should drop comet servers when a session is removed") {
			val session = Session.create.saveMe
			Session.addSession(session)	
			Session.removeSession(session)
			Session.getDisplayServer(Full(session.primaryKeyField)) should be (None)
		}
		it ("should log a student in to the correct session") {
		}
		it ("should logout a student from the correct session") {}
		it ("should find all sessions started by a user") {}
  }
}
