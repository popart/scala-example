package code 
package snippet 

import net.liftweb.mapper.{NullRef, By, NotBy, OrderBy, Ascending}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import code.model.{Click, Folder, Question, User}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JE._
import JsCmds._
import net.liftweb.widgets.tree.{Tree, TreeView}

class SharedTree {
	def rootUsers = User.findAll(NotBy(User.primaryKeyField, (User.currentUserId.map(_.toLong) openOr 0L)), OrderBy(User.lastName, Ascending))
	def rootFolders(u: User) = Folder.findAll(By(Folder.teacher, u), NullRef(Folder.parent))
	def folderToTree(f: Folder) = {
		Tree(f.title.is, f.primaryKeyField.toString, "", false, (!f.children.isEmpty || !f.questions.isEmpty), Nil)
	}
	def userToTree(u: User) = {
		Tree(u.lastName.is+", "+u.firstName.is, "U_"+u.primaryKeyField.toString, "", false, (!u.folders.isEmpty), Nil)
	}
	def render = { 
		def loadTree() = {
			Click.click("Shared Question Tree", "INIT_TREE", "")
			rootUsers.map(u => userToTree(u))
		}
		def loadNode(id:String) = {
			if(id.startsWith("U")) {
				val idNum = id.substring(2)
				val u = User.findByKey(idNum.toLong) openOr User.create
				Click.click("Shared Question Tree", "OPEN_USER", u.niceName)
				u.folders.map(f => folderToTree(f)).toList
			} else {
				val f = Folder.findByKey(id.toLong) openOr Folder.create
				Click.click("Shared Question Tree", "OPEN_FOLDER", f.title.is)
				((f.children map (fc => folderToTree(fc))) ++ (f.currentQuestions map (q => Tree(q.shortTextJSON)))).toList
			}
		}
		"#f_tree *" #> TreeView("f_tree", JsObj(("animated" -> 45)), loadTree, loadNode)
	}
}
