package code 
package snippet 

import net.liftweb.mapper.{NullRef, By}
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import code.lib._
import Helpers._
import code.model.{Folder, Question, User, Click}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JE._
import JsCmds._
import net.liftweb.widgets.tree.{Tree, TreeView}

/*
 * Uses Lift's TreeWidget to render out the folders and questions
 */
class FolderTree {
	def rootFolders = Folder.findAll(By(Folder.teacher, User.currentUser), NullRef(Folder.parent))
	/* sessionVar is cool but what if the directory changes after loading it? 
	 * it's no longer up to date that's what
	 */
	 /*
	object treeState extends SessionVar[List[Tree]](
		Folder.findAll(By(Folder.teacher, User.currentUser), NullRef(Folder.parent))
			.map(f => Tree(f.title.is, f.primaryKeyField.toString, (!f.children.isEmpty & !f.questions.isEmpty))) //true (hasChildren)
	)
	object treeState extends SessionVar[Map[Folder, Boolean]](
		folders.map(f => (f, false))
	)
	*/
	def folderToTree(f: Folder) = {
		Tree(f.title.is, f.primaryKeyField.toString, "", false, (!f.children.isEmpty || !f.questions.isEmpty), Nil)
	}
	def render = { 
		def loadTree() = {
			Click.click("Question Tree", "INIT_TREE", "")
			rootFolders.map(f => folderToTree(f))
			//treeState.is
			//Folder.findAll(By(Folder.teacher, User.currentUser), NullRef(Folder.parent)).map(f => Tree(f.title.is, f.primaryKeyField.toString, true))
		}
		//folder leaves not loaded until clicked
		def loadNode(id:String) = {
			val f = Folder.findByKey(id.toLong) openOr Folder.create
			Click.click("Question Tree", "OPEN_FOLDER", f.title.is)
			//((f.children map (fc => Tree(fc.title.is, fc.primaryKeyField.toString, "", false, (!f.children.isEmpty || !f.questions.isEmpty), Nil))) ++ (f.currentQuestions map (q => Tree(q.shortTextJSON)))).toList
			((f.children map (fc => folderToTree(fc))) ++ (f.currentQuestions map (q => Tree(q.shortTextJSON)))).toList

			//todo: save tree state in sessionvar and get rid of tabs!
			//var nodeTree = treeState.is.find(t => {t.id.map(_ == id) openOr false})
			//nodeTree.ca
		}
		"#f_tree *" #> TreeView("f_tree", JsObj(("animated" -> 45)), loadTree, loadNode)
	}
}
