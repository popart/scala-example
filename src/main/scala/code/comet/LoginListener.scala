package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import net.liftweb.util.TimeHelpers._
import scala.xml.NodeSeq
import code.model.{Session, User}


class LoginListener extends CometActor with CometListener with Loggable {
	var studentLog: List[LoginLine] = Nil
	def registerWith = {
		Session.getLoginServer(User.getSessionId) match {
			case Some(server) => {
				logger.debug("Found the LoginServer for session:"+(User.getSessionId.map(_.toString)  openOr "no session"))
				server
			}
			case None => {
				logger.debug("Did not find LoginServer for session:"+(User.getSessionId.map(_.toString)  openOr "no session"))
				new LoginServer
			}
		}
	}
	override def localShutdown = {
		logger.trace("Shutting down a LoginListener")
		super.localShutdown
	}
	override def localSetup = {
		logger.trace("Starting up a LoginListener")
		super.localSetup
	}
	//dies if you are not looking at the page, then refreshes and restarts
	override def lifespan = Full(seconds(60))

	override def lowPriority = {
		case LoginServerUpdate(l) =>
			logger.trace("LoginListener received an update")
			studentLog = l
			reRender
		case _ =>
	}

	private def line(l: LoginLine) = {
		(".name *" #> l.name &
		 ".name [style]" #> (if(l.loggedIn) "color:green" else "color:black"))
	}
	override def render = {
		logger.trace("LoginListener is rendering update")
		".student" #> studentLog.sortWith((a, b) => a.name < b.name).map(line)
	}
}
