package code
package model

import net.liftweb.mapper._
import net.liftweb.http.S
import java.util.Date

/*
 * This class logs user actions for UI analysis
 * All data is human read so just use sensible strings
 * Ex. Click.click("Teacher Login", "LOGIN_TEACHER", id.toString)
 */
object Click extends Click with LongKeyedMetaMapper[Click] {
	def click(page: String, action: String, input: String) = {
		super.create
			.webSession(S.session.map(_.uniqueId) openOr "")
			.teacher(User.currentUser)
			.classSession(User.getSession)
			.page(page)
			.action(action)
			.input(input)
			.dateTime(new Date)
			.save
	}
}

class Click extends LongKeyedMapper[Click] with IdPK {
	def getSingleton = Click

	object webSession extends MappedText(this) {
		override def dbColumnName = "web_session"
	}
	object teacher extends MappedLongForeignKey(this, User)
	object classSession extends MappedLongForeignKey(this, Session) {
		override def dbColumnName = "class_session"
	}
	object page extends MappedText(this)
	object action extends MappedText(this)
	object input extends MappedText(this)
	object dateTime extends MappedDateTime(this)
}
