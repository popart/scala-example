package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import scala.xml.NodeSeq

/*
 * Just lists the student roster and changes color when they log in
 */
class LoginServer extends LiftActor with ListenerManager {
	var students: Map[String, Boolean] = Map()

	override def lowPriority = {
		case LoginLine(studentName, loggedIn) =>
			students += studentName -> loggedIn
			updateListeners()	
		case LoginServerUpdate(msgs) => //if students.isEmpty => //init
			students = Map()
			msgs.map(m => students += m.name -> m.loggedIn)
			updateListeners()
	 	case _ =>
	}
	override def createUpdate = {
		LoginServerUpdate(students.map(s => LoginLine(s._1, s._2)).toList)
	}

}

/* Message sent from Session.loginStudent */
/* There aren't actually any loggedIn=false (logout) messages getting sent */
case class LoginLine(name: String, loggedIn: Boolean)	
/* Update message sent to listener */
case class LoginServerUpdate(msgs: List[LoginLine])
