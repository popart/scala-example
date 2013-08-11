package code 
package snippet 

import net.liftweb.mapper.{By, In, NotNullRef, OrderBy, Descending}
import scala.xml._
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import code.model.{Click, Session, SessionQuestions, Folder, Question, User, ImageInfo}
import net.liftweb.http._
import net.liftweb.sitemap._
import js._
import JsCmds._
import code.comet.{ResponseServer, ResponseServerUpdate, ResponseLine, QuestionServer, AskQ, DisplayServer, DisplayServerUpdate}

case class QuestionId(theId: String)

/*
 * How a teacher looks at a question
 */
class QuestionView(fi: QuestionId) {
	var daQuestion:Box[Question] = Question.findByKey(fi.theId.toLong)
	var disQuestion:Question = Question.findByKey(fi.theId.toLong) openOr Question.create
	var text = daQuestion.map(_.text.is) openOr "Question not Found"
	def render = {
		var img: ImageInfo = disQuestion.imageInfo openOr ImageInfo.create
		"#result" #> (try {XML.loadString("<span>"+text+"</span>")} catch { 
			case _ => <span>{text}</span>
		}) & (if(daQuestion.open_!.folder.isDefined)
			("#folder" #> <a href={"/folder/view/%s".format(daQuestion.open_!.folder.open_!.primaryKeyField.toString)}>{daQuestion.open_!.folder.open_!.title.is}</a>)
		 else "#folder" #> "No Folder"

		) &
		"#q_img" #> (if(disQuestion.imageInfo.map(_.url).isDefined)
			<img src={disQuestion.imageInfo.map(_.url).open_!} id="q_img"/>
			else <span></span>)
	}
	def ask = {
		def askQuestion() = {
			if(!User.getSession.isDefined){
				Click.click("Question View", "ASK_QUESTION", "FAIL")
				S.error("No class in session")
				S.redirectTo("/question/view/%s".format(fi.theId))
			}
			else {
			/*
				val daSession = User.getSession.open_!
				Session.getQuestionServer(User.getSessionId).map(_ ! AskQ(disQuestion.text, disQuestion.imageInfo.map(_.url), disQuestion.id, true))
				SessionQuestions.create.session(daSession).question(daQuestion.open_!).save
				Session.getResponseServer(User.getSessionId).map(_ ! ResponseServerUpdate(
				Nil))
				Session.getDisplayServer(User.getSessionId).map(_ ! DisplayServerUpdate(Nil))
				*/
				Question.stop()
				SessionQuestions.create.session(User.getSession.open_!)
					.question(daQuestion.open_!)
					.datetime(new Date)
					.save
				Session.getQuestionServer(User.getSessionId).map(_ ! AskQ(disQuestion.text, disQuestion.imageInfo.map(_.url), disQuestion.id, true, false))
				Click.click("Question View", "ASK_QUESTION", "")
				S.redirectTo("/question/admin?s=%d".format(User.getSessionId openOr "0"))
			}
		}
		"#ask_btn" #> 
			SHtml.submit("Ask", askQuestion)
	}
	def edit = {
		//editable if current (lesson or folder)
		if(daQuestion map (q => q.mostRecent_?) openOr false) {
			var newText = text
			def updateQuestion() = {
				var newQ = daQuestion
				if(newText != text & newQ.open_!.folder.isDefined) newQ = daQuestion.map(_.update(newText))
				//if folder is empty then just update text
				else newQ.open_!.text(newText).save
				Click.click("Question View", "UPDATE_QUESTION", "")
				S.redirectTo("/question/view/%s".format(newQ map (_.primaryKeyField.toString) openOr "error"))
			}
			"#edit_field" #> SHtml.textarea(text, newText = _) &
			"#edit_submit" #> SHtml.submit("Update Question", updateQuestion)
		//not editable if deprecated
		} else {
			"#edit_field" #> "" &
			"#edit_submit" #> SHtml.submit("See Current Version", () => S.redirectTo("/question/view/%s".format(daQuestion.map(_.mostRecent.primaryKeyField.toString) openOr 0)))
		}
	}

	def move = {
		var folders = Folder.findAll(By(Folder.teacher, User.currentUser))
		var selected = ""
		def moveQ() = {
			def saveFolderMove(disQ: Question) = {
				def nextOrder = {
					val otherFolderQs = Question.findAll(By(Question.folder, selected.toLong), OrderBy(Question.order, Descending))
					if(!otherFolderQs.toList.isEmpty) {
						otherFolderQs.toList.head.order.is + 1
					} else 0
				}
				disQ.folder(selected.toLong).order(nextOrder).save
			}
			val curTrunk:List[Question] = Question.findAll(By(Question.rootQ, daQuestion.open_!.rootQ))
			if (curTrunk != Nil) {
				curTrunk.map(saveFolderMove(_))
			} else {
				daQuestion.map(saveFolderMove(_))
			}
			Click.click("Question View", "MOVE_QUESTION_FOLDER", "")
			S.redirectTo("/question/view/%s".format(fi.theId))
		}
		"#folder_sel" #> SHtml.select(folders.map(f => 
			(f.primaryKeyField.toString, f.title.is)), daQuestion.map(_.folder.is.toString), selected = _) & 
		"#move_submit" #> SHtml.submit("Move to Folder", moveQ) 
	}

	def history = {
		var hists:List[Question] = daQuestion.map(_.history) openOr Nil
		".history" #> hists.map(h => <li>{h.text.is}</li>)
	}

	//don't render the edit stuff for shared questions
	def admin = {
		if((daQuestion.map(_.folder.map(_.teacher.toLong) openOr 0) openOr 0) != (User.currentUser.map(_.primaryKeyField.toLong) openOr -1)) ("#admin *" #> <span></span>)
		else "#not_here" #> <span></span>
	}
}
object QuestionViewParam {
	val menu = Menu.param[QuestionId]("Question Detail", "Question Detail",
																	s => Full(QuestionId(s)),
																	fi => fi.theId) / "question" / "view"
	lazy val loc = menu.toLoc
	def render = "*" #> loc.currentValue.map(_.theId)
}
