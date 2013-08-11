package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import scala.xml.NodeSeq

/*
 * This is here for teachers that have separate projection screens
 * Which ours don't, and I took the display page out of Boot
 */
class DisplayServer extends LiftActor with ListenerManager {
	var displays = Set[String]()

	override def lowPriority = {
		case DisplayLine(response, show) =>
			if(show) displays += response
			else displays -= response
			updateListeners()	
		case DisplayServerUpdate(msgs) => 
			displays = Set[String]() ++ msgs
			updateListeners()
	 	case _ =>
	}
	override def createUpdate = {
		DisplayServerUpdate(displays.toList)
	}

}

case class DisplayLine(response: String, show: Boolean)	
case class DisplayServerUpdate(msgs: List[String])
