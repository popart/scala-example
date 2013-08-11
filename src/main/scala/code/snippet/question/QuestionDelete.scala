package code
package snippet

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import code.model.{Click, Question}

/*
 * "Delete" a question and redirect
 */
object QuestionDelete {
	def render = {
		(S.param("id"), S.param("folder")) match {
			//delete from Folder
			case (Full(id:String), Full(folderId:String)) => {
				val question = Question.findByKey(id.toLong)
				question match {
					//you don't really delete from db, just clear the folder
					//so that it doesn't display in any lists
					//still non-recoverable from the teacher's pov
					case Full(q) => q.folder(Empty).save
					case _ => S.error("Could not find Folder or Question")
				}
				Click.click("Folder View", "DELETE_QUESTION", "")
				S.redirectTo("/folder/view/"+folderId)
			}
			case _ => {
				S.error("Invalid parameters")
				S.redirectTo("/")
			}
		}
	}
}
