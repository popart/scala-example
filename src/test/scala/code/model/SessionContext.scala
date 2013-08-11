package code
package model
  
import net.liftweb.http._
import net.liftweb.mapper._  
import net.liftweb.common._  
import net.liftweb.util._  
import code.model._  
import bootstrap.liftweb.Boot
import org.scalatest._

/*
 * Set up a session, test db, and log in a user 
 */
trait SessionContext extends AbstractSuite {  
	self: Suite =>
	//val session = new LiftSession("", StringHelpers.randomString(20), Empty)

	override abstract def withFixture(test: NoArgTest) = {
		try {
			(new Boot).boot 
			val session = new LiftSession("", StringHelpers.randomString(20), Empty)
			S.initIfUninitted(session) {
				SessionContext.super.withFixture(test)
			}
		}
		finally {
			//code here called after each test
			//pretty sure the db is only kept in memory so gets reallocated after each test
			S.session.foreach(_.destroySession)
		}
	}
}

object DBInit {
	def setupSession = {
		val user = createUser
		User.logUserIn(user)
		val section = Section.create
			.teacher(user)
			.saveMe
		val session = Session.create
			.section(section)
			.saveMe
		val student = Student.create
			.section(section)
			.saveMe
		Session.addSession(session)
		User.storeSession(session)
		Student.login(student)
	}
	def createUser = {
		User.create
			.firstName("XXX")
			.lastName("YYY")
		.saveMe
	}
	def createStudent = {
		Student.create
			.firstName("stud")
			.lastName("stud")
		.saveMe
	}
}
