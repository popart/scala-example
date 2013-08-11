package code
package model

import net.liftweb.mapper._
import net.liftweb.common._
import net.liftweb.http._

/*
 * Since both teachers and students need to manage a running class session
 * These functions are kept unified by storing here
 */
trait SessionHolder extends Loggable {

	private object curSessionId extends SessionVar[Box[Long]](Empty)
	private object curSession extends SessionVar[Box[Session]](Empty)
	def getSessionId:Box[Long] = curSessionId.is
	def getSession:Box[Session] = curSession.is
	def storeSession(s:Session) = {
		curSessionId.remove()
		curSession.remove()
		curSessionId(Full(s.primaryKeyField.is))
		curSession(Full(s))
		logger.debug("Stored Session in SessionVar:"+s.primaryKeyField.toString+
			", LiftSession id:"+S.session.map(_.uniqueId))
	}
	//gets called on user session timeouts by
	// lib.MySessionInfo -> Session -> Users
	def clearSession(kill: Boolean) = {
		if(kill) {
			Session.removeSession(getSession openOr Session.create)
			logger.debug("Called Session.removeSession on Session:"+(getSessionId.map(_.toString) openOr "no session"))
		}
		curSessionId.remove()
		curSession.remove()
		onClearSession
		logger.debug("Removed Session in SessionVar")
	}
	//This is a hook for teacher-specific or student-specific methods
	//Used by Student
	def onClearSession()
}
