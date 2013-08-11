package code
package model

import net.liftweb.mapper._
import net.liftweb.common._
import net.liftweb.util.FieldError
import scala.xml.Text
import net.liftweb.http._
import code.lib.StringCrypto

object Student extends Student with LongKeyedMetaMapper[Student] with SessionHolder with Loggable {
	private object curStudentId extends SessionVar[Box[String]](Empty) {
		//session doesn't get destroyed by closing the browser, eventually times out
		registerCleanupFunc(session => {
			logger.trace("StudentId SessionVar cleaned")
			curStudentId.is.map(sid => Session.logoutStudent(sid))
		})
	}
	private object curStudent extends SessionVar[Box[Student]](Empty) {
		registerCleanupFunc(session => {
			logger.trace("Student SessionVar cleaned")
		})
	}
	def currentStudentId:Box[String] = curStudentId.is
	def currentStudent:Box[Student] = curStudent.is
	def login(s:Student) = {
		curStudentId.remove()
		curStudent.remove()
		Session.managedLoginStudent(s) match {
			case true => {
				curStudentId(Full(s.userId.is))
				curStudent(Full(s))
			}
			case false => 
		}
	}
	override def onClearSession = {
		logger.debug("Clearing a student Session:"+(curStudentId.is openOr "no student"))
		curStudentId.remove()
		curStudent.remove()
		S.session.foreach(_.destroySession())
	}

	def logout() = {
		logger.debug("Logging out student:{"+
			(curStudent.map(_.primaryKeyField.toString) openOr "no student")+
			", "+(curStudent.map(_.lastName.is) openOr "no student")+"}"
		)
		clearSession(false)
	}
	def loggedIn_? = currentStudentId.isDefined
}

class Student extends LongKeyedMapper[Student] with OneToMany[Long, Student] with IdPK {
	def getSingleton = Student
	//DB LAG ?
	def validated_? = Session.findStudentSession(this).isDefined
	def testPassword(p:String) = p.equals(password.is)
	def displayName = lastName.is + ", " + firstName.is

	object section extends MappedLongForeignKey(this, Section)
	object lastName extends MappedString(this, 512) {
		override def set(s: String) = super.set(StringCrypto.encrypt(s))
		override def get(): String = StringCrypto.decrypt(super.get)
		override def is(): String = StringCrypto.decrypt(super.is)
	}
	object firstName extends MappedString(this, 512) {
		override def set(s: String) = super.set(StringCrypto.encrypt(s))
		override def get(): String = StringCrypto.decrypt(super.get)
		override def is(): String = StringCrypto.decrypt(super.is)
	}
	object userId extends MappedString(this, 512) {
		override def set(s: String) = super.set(StringCrypto.encrypt(s))
		override def get(): String = StringCrypto.decrypt(super.get)
		override def is(): String = StringCrypto.decrypt(super.is)
		override def validations = valUniqueEncrypted("This user ID is taken") _ :: super.validations
		// need to encrypt user id before checking uniqueness against db
		def valUniqueEncrypted(msg: => String)(value: String): List[FieldError] = {
			fieldOwner.getSingleton.findAll(By(this, StringCrypto.encrypt(value))).
			filter(!_.comparePrimaryKeys(this.fieldOwner)) match {
				case Nil => Nil
				case x :: _ => List(FieldError(this, Text(msg)))
			}
		}
	}
	object password extends MappedString(this, 32) 
	object responses extends MappedOneToMany(Response, Response.student, OrderBy(Response.date, Descending))
}
