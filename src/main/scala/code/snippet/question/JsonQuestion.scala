package code 
package snippet 

import net.liftweb._
import http._
import common._
import util._
import js._
import JsCmds._
import JE._
import scala.xml._
import code.model.{Student, Session}
import code.comet.{ResponseCloudServer, LiveUpdate}

object XString {
	def unapply(in: Any): Option[String] = in match {
		case s: String => Some(s)
		case _ => None
	}
}
object XArrayString {
	def unapply(in: Any): Option[List[String]] = in match {
		case lst: List[_] => 
			Some(lst.flatMap{
				case s:String => Some(s)
				case _ => None
			})
		case _ => None
	}
}

/* 
 * When the students press 'space' this creates the JS function
 * That will send their answer back to the server
 * for pre-processing
 */
object JsonQuestionHandler extends SessionVar[JsonHandler](
	new JsonHandler {
		def apply(in: Any): JsCmd = in match {
			//check out /index.html to see the liveUpdate call
			case JsonCmd("liveUpdate", _, XArrayString(s), _) =>
				//Send the string somewhere....
				val textCollector = Session.getResponseCloudServer(Student.getSessionId) getOrElse new ResponseCloudServer
				if(textCollector != null) {
					textCollector ! LiveUpdate(s(0).toLong, s(1))
				}
			case _ => Noop
		}
	}
)

/*
 * Dispatch snippets are the manual way to bind webpage renderings
 * Regular snippets create new instances for each page loada (class vs obj)
 * Here you need to explicitly map the html call to a function
 * And explicitly map LiftRules.dispatchSomething to the object (or you used to at least)
 */
object JsonQuestion extends DispatchSnippet {
	val dispatch = Map("render" -> buildFuncs _)

	def buildFuncs(in: NodeSeq): NodeSeq = 
	Script(JsonQuestionHandler.is.jsCmd &
		Function("liveUpdate", List("strArray"),
			JsonQuestionHandler.is.call("liveUpdate", JsVar("strArray"))
		)
	)
}

