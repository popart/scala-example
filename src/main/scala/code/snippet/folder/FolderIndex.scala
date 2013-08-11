package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Click, User, Folder}
import net.liftweb.http._
import scala.xml._
import js._
import JsCmds._
import JE._
import net.liftweb.mapper.{By, NullRef}

/*
 * About as basic an index as there is
 */
class FolderIndex {
	var folders = Folder.findAll(NullRef(Folder.parent), By(Folder.teacher, User.currentUser))

	def list = {
		".folder" #> folders.map ({ f =>
			def deleteFolder(): JsCmd = {
				Click.click("Question Folders", "DELETE_FOLDER", "")
				Confirm("Really Delete?", RedirectTo("/folder/delete/%s".format(f.primaryKeyField.toString)))
			}
			def deleteBtn = {
				if(f.questions.isEmpty && f.children.isEmpty) SHtml.ajaxButton("Delete", () => deleteFolder)
				else SHtml.ajaxButton("Delete", () => Alert("Cannot delete a folder with questions"))
			}
			".fTitle [href]" #> "/folder/view/%s".format(f.primaryKeyField.toString) & 
			".fTitle *" #> f.title.is &
			".fDelete " #> deleteBtn &
			".fGo " #> SHtml.ajaxButton("Add Questions", () => {
				Click.click("Question Folders", "ADD_QUESTIONS_BTN", "")
				RedirectTo("/folder/view/%s".format(f.primaryKeyField.toString))
			})
		})
	}

	def create = {
		var fTitle = ""
		def createFolder(): Unit = {
			if(fTitle.trim.length > 0) {
				Folder.create
					.title(fTitle).teacher(User.currentUser)
					.save
				Click.click("Question Folders", "CREATE_FOLDER", fTitle)
				S.redirectTo("/folder/")
			}
		}
		"#createF_title" #> SHtml.text("", fTitle = _, "size" -> "20") &
		"#createF_submit" #> SHtml.submit("Create Folder", createFolder)
	}
}
