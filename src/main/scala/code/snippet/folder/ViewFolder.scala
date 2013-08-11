package code 
package snippet 

import net.liftweb.mapper.{By, NotBy, OrderBy, Descending}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Click, Folder, Question, User}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JsCmds._

case class FolderId(theId: String)

class FolderView(fi: FolderId) {
	var curFolder = Folder.findByKey(fi.theId.toLong)
	var questions:List[Question] = curFolder.map(_.currentQuestions) openOr Nil
	var subfolders:List[Folder] = curFolder.map(_.children.toList) openOr Nil
	var title = curFolder.map(_.title.is) openOr "Folder not Found"
	def render = "#result *" #> title

  /* Render the list of questions */
	def qList = {
		".question" #> questions.map ({ q:Question =>
			def deleteQ(): JsCmd = {
				Confirm("Really Delete?", RedirectTo("/question/delete?id=%s&folder=%s".format(q.primaryKeyField.toString, fi.theId)))
			}
			def bumpQ(up: Boolean) = {
				q.bumpOrder(up)
				RedirectTo("/folder/view/%s".format(fi.theId))
			}
			def deleteBtn = SHtml.ajaxButton("Delete",() => deleteQ, "type" -> (if(q.blockDelete) "hidden" else "")) 
			".qText [href]" #> "/question/view/%s".format(q.primaryKeyField.toString) & 
			".qText *" #> q.text.is & //q.shortText &
			".qText [target]" #> "_blank" &
			".qFolder *" #> q.folder.obj.map(_.title.is) &
			".qDelete " #> deleteBtn &
			".qUp" #> SHtml.ajaxButton("^", () => bumpQ(true)) &
			".qDown" #> SHtml.ajaxButton("v", () => bumpQ(false)) 
		})
	}
	
	/* Create a new question */
	def qCreate = {
		var qText = ""
		def createQ(): Unit = {
			if(qText.trim.length > 0) {
				def nextOrder = {
					val otherFolderQs = Question.findAll(By(Question.folder, curFolder), OrderBy(Question.order, Descending))
					if(!otherFolderQs.toList.isEmpty) {
						otherFolderQs.toList.head.order.is + 1
					} else 0
				}

				val newQ = Question.create
				newQ.text(qText)
					.folder(curFolder)
					.order(nextOrder).save
				//need to save first to have an id 
				//before able to set root as self
				newQ.rootQ(newQ).save
				Click.click("Folder View", "CREATE_QUESTION", "")
				S.redirectTo("/folder/view/%s".format(fi.theId))
			}
		}
		"#createQ_text" #> SHtml.textarea("", qText = _) &
		"#createQ_submit" #> SHtml.submit("Create Question", createQ)
	}

 	/* List subfolders */
	def fList = {
		".folder" #> subfolders.map ({ q:Folder =>
			def deleteQ(): JsCmd = {
				Click.click("Folder View", "DELETE_SUBFOLDER", "")
				Confirm("Really Delete Folder?", RedirectTo("/folder/delete/%s".format(q.primaryKeyField.toString)))
			}
			def deleteBtn = SHtml.ajaxButton("Delete",() => deleteQ, "type" -> (if(q.questions.isEmpty && q.children.isEmpty) "hidden" else "")) 
			".fText [href]" #> "/folder/view/%s".format(q.primaryKeyField.toString) & 
			".fText *" #> q.title.is &
			".fDelete " #> deleteBtn
		})
	}
	
	def fCreate = {
		var fText = ""
		def createF(): Unit = {
			if(fText.trim.length > 0) {
				val newF = Folder.create
				newF.title(fText).teacher(User.currentUser).parent(curFolder).save
				Click.click("Folder View", "CREATE_SUBFOLDER", fText)
				S.redirectTo("/folder/view/%s".format(fi.theId))
			}
		}
		"#createF_text" #> SHtml.text("", fText = _) &
		"#createF_submit" #> SHtml.submit("Create Subfolder", createF)
	}

	def move = {
		def checkIfChild(dis: Folder, child: Folder): Boolean = {
			val childrens = dis.children.toList
			childrens match {
				case Nil => false
				case _ => childrens.contains(child) || childrens.exists(cx => checkIfChild(cx, child))
				//pretty sure this is tail-recursive
			}
		}

		var folders = Folder.findAll(By(Folder.teacher, User.currentUser))
			.filterNot(_ == (curFolder openOr new Folder))
			.filterNot(xf => checkIfChild((curFolder openOr new Folder), xf))
		var selected = ""
		def moveF() = {
			if(selected != "0") curFolder.map(_.parent(selected.toLong).save)
			else curFolder.map(_.parent(Empty).save)
			Click.click("Folder View", "MOVE_SUBFOLDER", "")
			S.redirectTo("/folder/view/%s".format(fi.theId))
		}
		"#folder_sel" #> SHtml.select(("0", "-") :: folders.map(f => 
			(f.primaryKeyField.toString, f.title.is)), curFolder.map(_.parent.is.toString), selected = _) & 
		"#move_submit" #> SHtml.submit("Move to Folder", moveF) 
	}

}
object FolderViewParam {
	val menu = Menu.param[FolderId]("Folder Detail", "Folder Detail",
																	s => Full(FolderId(s)),
																	fi => fi.theId) / "folder" / "view"
	lazy val loc = menu.toLoc
	def render = "*" #> loc.currentValue.map(_.theId)
}
class FolderDelete(fi: FolderId) {
	def render = {
		var curFolder = Folder.findByKey(fi.theId.toLong)
		curFolder match {
			case Full(f) => {
				if(f.questions.isEmpty) {
					f.delete_!	
					S.redirectTo("/folder/")
				} else {
					Click.click("?", "DELETE_FOLDER", "FAIL")
					S.error("Must clear questions before deleting a Folder")
					S.redirectTo("/folder/")
				}
			}
			case _ => {
				S.error("Could not find Folder")
				S.redirectTo("/folder/")
			}
		}
	}
}
object FolderDelParam {
	val menu = Menu.param[FolderId]("folder_del", "folder_del",
																	s => Full(FolderId(s)),
																	fi => fi.theId) / "folder" / "delete"
	lazy val loc = menu.toLoc
	def render = "*" #> loc.currentValue.map(_.theId) 
}
