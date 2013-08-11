package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import net.liftweb.util.TimeHelpers.seconds
import scala.xml.NodeSeq
import js.JsCmd
import js.JsCmds.{SetHtml, Noop}
import js.jquery.JqJsCmds.AppendHtml
import code.model.{User, Session}

class ResponseCloudListener extends CometActor with CometListener {
	object words extends SessionVar[List[(String, Float)]](Nil)

	def registerWith = Session.getResponseCloudServer(User.getSessionId) getOrElse
		(new ResponseCloudServer)
	override def lifespan = Full(seconds(60))

	override def lowPriority = {
		case ResponseCloudUpdate(weightedWords) =>
			words(weightedWords)
			partialUpdate(
				SetHtml("response_cloud", <p>{
					for ((word, weight) <- words.is)
					yield <xml:group>
						<span style={"font-size:"+scala.math.min(10, (1.0F + 10*weight))+"em; margin:0 0.2em"}>{
							word}</span>
					</xml:group>}</p>)
			) 
		case _ =>
	}

	override def render = {
		"#response_cloud *" #> <p>{
			for ((word, weight) <- words.is)
			yield <xml:group>
				<span style={"font-size:"+(1.5F + 5*weight)+"em;"}>{
					word}</span>
			</xml:group>
		}</p> 
	}
}
