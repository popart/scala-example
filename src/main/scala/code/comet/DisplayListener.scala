package code
package comet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.actor._
import net.liftweb.util.TimeHelpers._
import js.JsCmd
import js.JsCmds._
import js.jquery.JqJsCmds._
import scala.xml.NodeSeq
import code.model.{User, Session, Click}

class DisplayListener extends CometActor with CometListener {
	var displays: List[String] = Nil
	def registerWith = Session.getDisplayServer(User.getSessionId) getOrElse 
		(new DisplayServer)
	override def lifespan = Full(seconds(60))

	def closeResponse(d: String): JsCmd = {
		Click.click("Display", "CLOSE_RESPONSE", "")
		Session.getDisplayServer(User.getSessionId).map(_ ! DisplayLine(d, false))
		Noop
	}

	override def lowPriority = {
		case DisplayServerUpdate(d) =>
			//val closed = displays.diff(d)
			//val added = d.diff(displays)
			displays = d

			reRender
			/*
			partialUpdate(
				Alert("displays: "+displays.toString) &
				Alert("added: "+added.toString) &
				Alert("closed: "+closed.toString) &
				//SetHtml("display_anchor", NodeSeq.Empty) &
				added.map(u => {
					AppendHtml("display_anchor", 
						<div class="r_txtbox span-8" style="background:pink; border: 1px solid" id={"r"+u.hashCode}>
						<span class="txt">{u}</span><br/>
						{SHtml.ajaxButton("Close", () => closeResponse(u))}	
					</div>)
				}) &
				closed.map(c => js.JE.Call("$('#r"+c.hashCode+"').remove()").cmd) & 
				js.JE.Call("addUI").cmd
			)
			*/
		case _ =>
	}

	override def render = {
		S.appendJs(js.JE.Call("addUI").cmd) 
		".r_txtbox " #> displays.map(d => {
			".txt" #> d &
			".close_btn" #> SHtml.ajaxButton("Close", () => closeResponse(d))
		})
	}
}
