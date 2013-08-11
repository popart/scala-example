package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import scala.xml.NodeSeq

/*
 * Receives student responses when they press Enter
 * Sends to ResponseListener to display on question/admin.html
 */
class ResponseServer extends LiftActor with ListenerManager {
  var students: Map[String, (Boolean, Int)] = Map()
	var studentResponses: Map[String, String] = Map()

	override def lowPriority = {
		case ResponseLine(studentName, response, submitted, _) =>
			if(students.get(studentName).isEmpty) {
				students += studentName -> (submitted, students.size+1)
			}
			studentResponses += studentName -> response
			updateListeners()	
		case ResponseServerUpdate(msgs) =>
			students = Map()
			studentResponses = Map()
			msgs.map(m => students += m.name -> (m.submitted, m.order))
			updateListeners()
	 	case _ =>
	}
	override def createUpdate = {
		ResponseServerUpdate(students.map(s => 
			ResponseLine(
				s._1, 
				(studentResponses.get(s._1) getOrElse "Waiting for Response"),
				s._2._1,
				s._2._2
			)).toList)
	}

}

/* So that there is a total count there's a new submitted=false message 
 * for every student whenever a question gets asked */
case class ResponseLine(name: String, response: String, submitted: Boolean, order: Int) 
case class ResponseServerUpdate(msgs: List[ResponseLine])
