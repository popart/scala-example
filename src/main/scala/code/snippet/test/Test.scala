package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{User, Session}
import net.liftweb.http._
import scala.xml._
import js._
import JsCmds._
import JE._
import code.comet.{LoginServer, LoginServerUpdate, LoginLine}

/* 
 * Just for development
 */
class TestSnippet {
	val su = Props.get("admin.username") openOr "error"
	val sp = Props.get("admin.password") openOr "error"
	val run = System.getProperty("run.mode")

	def render = {
		println("***** TEST *****")
		println(" current user: "+(User.currentUser.map(_.hashCode.toString) openOr "no user"))
		println("****************")
		"#username " #> su &
		"#password" #> sp &
		"#resource_path" #> ResourceServer.baseResourceLocation &
		"#run_mode" #> run &
		"#current_user" #> User.currentUser.map(_.email.is) &
		"#current_session" #> (User.getSession.map(_.section.map(_.title.is) openOr "what1") openOr "what2") &
		"#students *" #> (User.getSession.map(_.section.map(_.students.toList) openOr Nil) openOr Nil).map(s => {
			".s_name *" #> s.displayName
		})
	}
}
